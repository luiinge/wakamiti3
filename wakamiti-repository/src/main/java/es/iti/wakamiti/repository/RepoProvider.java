package es.iti.wakamiti.repository;

import java.nio.file.Path;
import java.sql.SQLException;

public class RepoProvider {

	public static void main(String[] args) throws SQLException {
		Path file = Path.of("build/repo/data").toAbsolutePath();
		RepositoryServer server = null;
		try {
			server = new RepositoryServer(file);
			server.start();
		} finally {
			if (server != null) {
				server.stop();
			}
		}
	}

}
