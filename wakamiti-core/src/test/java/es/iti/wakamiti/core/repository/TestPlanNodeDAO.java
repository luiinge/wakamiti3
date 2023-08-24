package es.iti.wakamiti.core.repository;

import es.iti.wakamiti.core.DefaultIDProvider;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.core.WakamitiProperties;
import imconfig.Config;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class TestPlanNodeDAO {

	@Test
	void testPlanNodeDAO() {

		NitriteRepository repo = new NitriteRepository();
		repo.config = Config.factory().fromPairs(
			WakamitiProperties.REPOSITORY_TRANSIENT, "true"
		);
		repo.idProvider = new DefaultIDProvider();


		PlanNodeDAO root = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("root"));
		PlanNodeDAO a = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("a"));
		PlanNodeDAO a_1 = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("a_1"));
		PlanNodeDAO a_2 = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("a_2"));
		PlanNodeDAO a_3 = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("a_3"));
		PlanNodeDAO b = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("b"));
		PlanNodeDAO b_1 = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("b_1"));
		PlanNodeDAO b_2 = PlanNodeDAO.persist(repo,new PlanNode(NodeType.AGGREGATOR).name("b_2"));

		assertThat(root.node().name()).isEqualTo("root");

		root.attachChild(a);
		repo.checkIntegrity();
		a.attachChild(a_1);
		repo.checkIntegrity();
		a.attachChild(a_2);
		repo.checkIntegrity();
		a.attachChild(a_3);
		repo.checkIntegrity();
		root.attachChild(b);
		repo.checkIntegrity();
		b.attachChild(b_1);
		repo.checkIntegrity();
		b.attachChild(b_2);
		repo.checkIntegrity();

		assertThat(root.getChildren().stream().map(PlanNodeDAO::nodeID)).containsExactly(a.nodeID(),b.nodeID());
		assertThat(a.getParent().orElseThrow().nodeID()).isEqualTo(root.nodeID());
		assertThat(b.getParent().orElseThrow().nodeID()).isEqualTo(root.nodeID());

		assertThat(a.getChildren().stream().map(PlanNodeDAO::nodeID))
			.containsExactly(a_1.nodeID(),a_2.nodeID(),a_3.nodeID());
		assertThat(b.getChildren().stream().map(PlanNodeDAO::nodeID))
			.containsExactly(b_1.nodeID(),b_2.nodeID());

		assertThat(a_1.getAncestors().map(PlanNodeDAO::nodeID))
			.containsExactly(a.nodeID(),root.nodeID());
		assertThat(a_2.getAncestors().map(PlanNodeDAO::nodeID))
			.containsExactly(a.nodeID(),root.nodeID());
		assertThat(a_3.getAncestors().map(PlanNodeDAO::nodeID))
			.containsExactly(a.nodeID(),root.nodeID());

		assertThat(b_1.getAncestors().map(PlanNodeDAO::nodeID))
			.containsExactly(b.nodeID(),root.nodeID());
		assertThat(b_2.getAncestors().map(PlanNodeDAO::nodeID))
			.containsExactly(b.nodeID(),root.nodeID());

		a.deleteChild(a_2);
		repo.checkIntegrity();
		a.deleteChild(a_3);
		repo.checkIntegrity();

		assertThat(a.getChildren().stream().map(PlanNodeDAO::nodeID)).containsExactly(a_1.nodeID());

		root.deleteChild(a);
		repo.checkIntegrity();
		root.deleteChild(b);
		repo.checkIntegrity();




	}
}
