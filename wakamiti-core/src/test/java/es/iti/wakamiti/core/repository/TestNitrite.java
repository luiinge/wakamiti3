package es.iti.wakamiti.core.repository;

import es.iti.wakamiti.core.DefaultIDProvider;
import es.iti.wakamiti.api.plan.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.*;

import imconfig.Config;

public class TestNitrite {

	static final NitriteRepository repo = new NitriteRepository();

	@BeforeAll
	static void init() {
		repo.config = Config.factory().fromEnvironment();
		repo.idProvider = new DefaultIDProvider();
	}


	@Test
	void testNitrite() {

		PlanNodeID root = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("root"));
		PlanNodeID a = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("a"));
		PlanNodeID a_1 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("a_1"));
		PlanNodeID a_2 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("a_2"));
		PlanNodeID a_3 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("a_3"));
		PlanNodeID b = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("b"));
		PlanNodeID b_1 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("b_1"));
		PlanNodeID b_2 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("b_2"));

		assertThat(repo.get(root).orElseThrow().name()).isEqualTo("root");

		repo.attachChild(root,a);
		repo.attachChild(a,a_1);
		repo.attachChild(a,a_2);
		repo.attachChild(a,a_3);
		repo.attachChild(root,b);
		repo.attachChild(b,b_1);
		repo.attachChild(b,b_2);

		assertThat(repo.getChildren(root)).containsExactly(a,b);
		assertThat(repo.getChildren(a)).containsExactly(a_1,a_2,a_3);
		assertThat(repo.getChildren(b)).containsExactly(b_1,b_2);

		assertThat(repo.getAncestors(a_1)).containsExactly(a,root);
		assertThat(repo.getAncestors(a_2)).containsExactly(a,root);
		assertThat(repo.getAncestors(a_3)).containsExactly(a,root);

		assertThat(repo.getAncestors(b_1)).containsExactly(b,root);
		assertThat(repo.getAncestors(b_2)).containsExactly(b,root);

		repo.delete(a_2);
		assertThat(repo.getChildren(a)).containsExactly(a_1,a_3);

		repo.detachChild(a,a_3);
		assertThat(repo.getChildren(a)).containsExactly(a_1);


	}


	@Test
	void canMoveNodeToAnotherBranch() {

		PlanNodeID root = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("root"));
		PlanNodeID a = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("a"));
		PlanNodeID a_1 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("a_1"));
		PlanNodeID a_2 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("a_2"));
		PlanNodeID b = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("b"));
		PlanNodeID b_1 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("b_1"));
		PlanNodeID b_2 = repo.persist(new PlanNode(NodeType.AGGREGATOR).name("b_2"));

		repo.attachChild(root, a);
		repo.attachChild(a, a_1);
		repo.attachChild(a, a_2);
		repo.attachChild(root, b);
		repo.attachChild(b, b_1);
		repo.attachChild(b, b_2);

		repo.detachChild(b, b_1);
		repo.checkIntegrity();
		repo.attachChild(a, b_1);
		repo.checkIntegrity();

		assertThat(repo.getChildren(a)).containsExactly(a_1, a_2, b_1);
		assertThat(repo.getChildren(b)).containsExactly(b_2);
	}
}
