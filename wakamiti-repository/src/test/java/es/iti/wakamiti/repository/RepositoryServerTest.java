package es.iti.wakamiti.repository;

import es.iti.wakamiti.api.*;
import java.io.IOException;
import java.nio.file.*;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;

class RepositoryServerTest {

	@Test
	@DisplayName("a new repository server can be created from a non-existing path")
	void newRepositoryServer() throws IOException {
		Path file = Files.createTempDirectory("test");
		Files.createDirectories(file);
		RepositoryServer server = new RepositoryServer(file);
		try {
			server.start();
			var session = new Session(server::newConnection, Log.of());
			Assertions.assertTrue(session.exists("SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'PLAN'"));
		} finally {
			server.stop();
		}
	}



	@Test
	@DisplayName("the persistence file store information after closing")
	void checkDataStoredAfterClosed() throws IOException, SQLException {
		Path file = Files.createTempDirectory("test");
		Files.createDirectories(file);
		RepositoryServer firstInstance = new RepositoryServer(file);
		UUID planUUID = UUID.randomUUID();
		try {
			firstInstance.start();
			try (var session = new Session(firstInstance::newConnection, Log.of())) {
				session.execute(
					"INSERT INTO PLAN (PLAN_ID,ORGANIZATION,PROJECT,NAME,HASH) VALUES (?1,?2,?3,?4,?5)",
					planUUID,
					"test",
					"test",
					"test",
					"test"
				);
				session.commit();
			}
		} finally {
			firstInstance.stop();
		}
		RepositoryServer secondInstance = new RepositoryServer(file);
		try {
			secondInstance.start();
			try (var session = new Session(firstInstance::newConnection, Log.of())) {
				Assertions.assertTrue(session.exists(
					"SELECT * FROM PLAN WHERE PLAN_ID = ?1",
					planUUID
				));
			}
		} finally {
			secondInstance.stop();
		}
	}


	@Test
	@DisplayName("several connections can be opened")
	void severalConnectionsCanBeOpened() throws IOException, SQLException {
		Path file = Files.createTempDirectory("test");
		Files.createDirectories(file);
		RepositoryServer server = new RepositoryServer(file);

		try {
			server.start();
			Runnable runnable = () -> {
				try (var session = new Session(server::newConnection, Log.of())) {
					session.execute(
						"INSERT INTO PLAN (PLAN_ID,ORGANIZATION,PROJECT,NAME,HASH) VALUES (?1,?2,?3,?4,?5)",
						UUID.randomUUID(),
						"test",
						"test",
						"test",
						"test"
					);
					session.commit();
				}
			};
			var threadPool = Executors.newFixedThreadPool(3);
			threadPool.submit(runnable);
			threadPool.submit(runnable);
			threadPool.submit(runnable);
			threadPool.shutdown();
			Assertions.assertTrue(threadPool.awaitTermination(10,TimeUnit.SECONDS));

			try (var session = new Session(server::newConnection, Log.of())) {
				int inserted = session.single(r -> r.getInt(1), "SELECT COUNT(*) FROM PLAN");
				Assertions.assertEquals(3,inserted);
			}

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new WakamitiException(e);
		} finally {
			server.stop();
		}
	}


	@Test
	@DisplayName("data not persisted if commit not called")
	void checkDataIsNotPersistedIfCommitNotCalled() throws IOException, SQLException {
		Path file = Files.createTempDirectory("test");
		Files.createDirectories(file);
		RepositoryServer server = new RepositoryServer(file);
		try {
			server.start();
			try (var session = new Session(server::newConnection, Log.of())) {
				session.execute(
					"INSERT INTO PLAN (PLAN_ID,ORGANIZATION,PROJECT,NAME,HASH) VALUES (?1,?2,?3,?4,?5)",
					UUID.randomUUID(),
					"test",
					"test",
					"test",
					"test"
				);
			} // session is closed wihtout commit
			try (var session = new Session(server::newConnection, Log.of())) {
				Assertions.assertFalse(session.exists("SELECT * FROM PLAN"));
			}
		} finally {
			server.stop();
		}

	}




}
