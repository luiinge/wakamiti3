package es.iti.wakamiti.repository;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import javax.sql.DataSource;

import org.hsqldb.Server;
import org.hsqldb.jdbc.JDBCDataSource;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl;

import es.iti.wakamiti.api.*;
import jexten.Version;

public class RepositoryServer {

	private static final Log log = Log.of();

	private static final String USER = "wakamiti";
	private static final String PWD = "wakamiti";
	private static final String SCHEMA = "WAKAMITI";

	private final DataSource dataSource;
	private final Server dbServer;


	public RepositoryServer(Path file) {
		int port = freePort();
		this.dataSource = createDataSource(port);
		this.dbServer = createServer(file,port);
	}



	public void start() {
		dbServer.start();
		prepareDatabase();
	}


	public void stop() {
		dbServer.stop();
	}


	private static Server createServer(Path file, int port) throws WakamitiException {
		HsqlProperties serverProperties = new HsqlProperties();
		serverProperties.setProperty("server.database.0", "file:%s;user=%s;password=%s".formatted(file,USER,PWD));
		serverProperties.setProperty("server.dbname.0",SCHEMA);
		serverProperties.setProperty("server.port",port);
		Server dbServer = new Server();
		try {
			dbServer.setProperties(serverProperties);
			return dbServer;
		} catch (IOException | ServerAcl.AclFormatException e) {
			throw new WakamitiException(e);
		}
	}


	public Connection newConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	private static DataSource createDataSource(int port) {
		JDBCDataSource dataSource = new JDBCDataSource();
		dataSource.setURL("jdbc:hsqldb:hsql://localhost:"+port);
		dataSource.setUser(USER);
		dataSource.setPassword(PWD);
		dataSource.setDatabase(SCHEMA);
		return dataSource;
	}


	private void prepareDatabase() {
		try (var session = new Session(this::newConnection)) {
			initDatabase(session);
			List<Version> appliedPatches = findAppliedPatches(session);
  		    Map<Version,Path> pendingPatches = findPendingPatches(appliedPatches);
			applyPendingPatches(session, pendingPatches);
			session.commit();
		} catch (SQLException e) {
			throw new WakamitiException(e);
		}
	}


	private void initDatabase(Session session) throws SQLException {
		if (!session.exists("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_CAT = ?1", SCHEMA)) {
			log.info("First time using the data repository, preparing the database...");
			session.execute(List.of(
				"ALTER CATALOG PUBLIC RENAME TO "+SCHEMA,
				"ALTER SCHEMA PUBLIC RENAME TO "+SCHEMA,
				"CREATE TABLE DBCHANGELOG (VERSION VARCHAR(20), TIMESTAMP TIMESTAMP, PRIMARY KEY (VERSION))"
			));
		}
	}


	private void applyPendingPatches(Session session, Map<Version,Path> pendingPatches)
	throws SQLException {
		try {
			List<String> statements = new LinkedList<>();
			for (var pendingPatch : pendingPatches.entrySet()) {
				Version version = pendingPatch.getKey();
				Path file = pendingPatch.getValue();
				log.info("Applying version {} patch to data repository", version);
				statements.addAll(Arrays.asList(Files.readString(file).split(";")));
				statements.add("INSERT INTO DBCHANGELOG (VERSION) VALUES ('"+version+"')");
			}
			session.execute(statements);
		} catch (IOException e) {
			throw new WakamitiException(e);
		}
	}





	private List<Version> findAppliedPatches(Session session) throws SQLException {
		return session.list(
			resultSet -> Version.of(resultSet.getString(1)),
			"SELECT VERSION FROM DBCHANGELOG"
		);
	}


	private Map<Version,Path> findPendingPatches(List<Version> appliedPatches) {
		try {
			SortedMap<Version,Path> pendingPatches;
			URL sqlFolderURL = RepositoryServer.class.getClassLoader().getResource("sql");
			if (sqlFolderURL == null) {
				return Map.of();
			}
			Path sqlFolder = Path.of(sqlFolderURL.toURI()).toAbsolutePath();
			try (Stream<Path> files = Files.walk(sqlFolder)) {
				pendingPatches = files
					.filter(Files::isRegularFile)
					.map(file -> Map.entry(Version.of(filename(file)),file))
					.filter(entry -> !appliedPatches.contains(entry.getKey()))
					.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(a, b)->a,
						TreeMap::new
					));
			}
			return pendingPatches;
		} catch (IOException | URISyntaxException e) {
			throw new WakamitiException(e);
		}
	}




	private static String filename(Path path) {
		String name = path.getFileName().toString();
		return name.substring(0, name.lastIndexOf('.'));
	}


	private static int freePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new WakamitiException(e);
		}
	}


}
