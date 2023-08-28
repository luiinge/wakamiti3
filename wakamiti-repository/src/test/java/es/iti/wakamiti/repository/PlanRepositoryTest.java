package es.iti.wakamiti.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

import org.junit.jupiter.api.*;

import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.repository.PlanRepository;

class PlanRepositoryTest {

	RepositoryServer server;

	@BeforeEach
	void startServer() throws IOException {
		Path file = Files.createTempDirectory("test");
		Files.createDirectories(file);
		server = new RepositoryServer(file);
		server.start();
	}


	@AfterEach
	void stopServer() {
		server.stop();
	}


	@Test
	void storeNewPlanNode() throws IOException, SQLException {
		try (var repo = createRepository()) {
			PlanNode node = node();
			UUID id = repo.persistNode(node);
			assertThat(id).isNotNull();
			assertThat(repo.existsNode(id)).isTrue();
			PlanNode retrieved = repo.getNode(id).orElseThrow();
			assertThat(retrieved).isEqualTo(node);
		}
	}


	@Test
	void updatePlanNode() throws IOException, SQLException {
		try (var repo = createRepository()) {
			PlanNode node = node();
			UUID id = repo.persistNode(node);
			PlanNode retrieved = repo.getNode(id).orElseThrow();
			retrieved.name("modified name");
			repo.persistNode(retrieved);
			PlanNode updated = repo.getNode(id).orElseThrow();
			assertThat(updated).isEqualTo(retrieved);
		}
	}


	@Test
	void newNodeHasNoRelationships() throws IOException, SQLException {
		try (var repo = createRepository()) {
			PlanNode node = node();
			UUID id = repo.persistNode(node);
			assertThat(repo.getParentNode(id)).isEmpty();
			assertThat(repo.getNodeChildren(id)).isEmpty();
			assertThat(repo.getNodeAncestors(id)).isEmpty();
			assertThat(repo.getNodeDescendants(id)).isEmpty();
			assertThat(repo.getRootNodeID(id)).hasValue(id);
			assertThat(repo.getRootNode(id)).hasValue(node);
		}
	}



	@Test
	void treeStructure() throws IOException, SQLException {
		try (var repo = createRepository()) {
			UUID root = repo.persistNode(new PlanNode(NodeType.STEP).name("root"));
			UUID a = repo.persistNode(new PlanNode(NodeType.STEP).name("a"));
			UUID b = repo.persistNode(new PlanNode(NodeType.STEP).name("b"));
			UUID a1 = repo.persistNode(new PlanNode(NodeType.STEP).name("a1"));
			UUID a2 = repo.persistNode(new PlanNode(NodeType.STEP).name("a2"));
			UUID a11 = repo.persistNode(new PlanNode(NodeType.STEP).name("a11"));
			UUID a12 = repo.persistNode(new PlanNode(NodeType.STEP).name("a12"));
			/*
			root
			 - a
			 */
			repo.attachChildNode(root,a);
			repo.commit();
			assertThat(repo.getRootNodeID(a)).contains(root);
			assertThat(repo.getParentNodeID(a)).contains(root);
			assertThat(repo.getNodeAncestorsID(a)).contains(root);
			assertThat(repo.getNodeChildrenID(root)).contains(a);
			assertThat(repo.getNodeDescendantsID(root)).contains(a);

		} finally {
			try (var c = server.newConnection()) {
				var q = c.prepareStatement("select * from plan_node_hierarchy").executeQuery();
				while (q.next()) {
					ResultSetMetaData metaData = q.getMetaData();
					int columnCount = metaData.getColumnCount();
					for (int i = 1; i <= columnCount; i++) {
						String columnName = metaData.getColumnName(i);
						Object value = q.getObject(i);
						System.out.print(columnName + ": " + value+"   ");
					}
					System.out.println();
				}
			}
		}
	}



	private void check(Function<UUID,Optional<UUID>> operation, UUID root, UUID... nodes) {
		Arrays.stream(nodes).forEach(node -> assertThat(operation.apply(node)).contains(root));
	}


	private void assertRootNode(PlanRepository repo, UUID root, UUID... nodes) {
		Arrays.stream(nodes).forEach(node -> assertThat(repo.getRootNodeID(node)).contains(root));
	}

	private PlanNode node() {
		PlanNode node = new PlanNode(NodeType.STEP);
		node.addTags(List.of("tag1", "tag2"));
		node.addProperty("property1", "value1").addProperty("property2", "value2");
		node.name("node name");
		node.keyword("when");
		node.description("description");
		node.identifier("my test");
		node.source("file.txt");
		node.displayNamePattern("{pattern}");
		node.language("en");
		node.document(new Document("json", """
			{ "data": "test" }
			"""));
		node.dataTable(new DataTable(List.of(
			List.of("c1", "c2"),
			List.of("c1a", "c2a"),
			List.of("c1b", "c2b")
		)));
		return node;
	}


	private RelationalPlanRepository createRepository() throws IOException {
		return new RelationalPlanRepository(server);
	}

}