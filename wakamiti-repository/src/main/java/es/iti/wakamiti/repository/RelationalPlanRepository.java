package es.iti.wakamiti.repository;


import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.repository.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

public class RelationalPlanRepository implements PlanRepository, AutoCloseable {

	private static final Log log = Log.of();
	private final Session session;


	public RelationalPlanRepository(RepositoryServer server) {
		this.session = new Session(server::newConnection,log);
	}


	@Override
	public void close() throws SQLException {
		this.session.close();
	}



	@Override
	public Optional<PlanNode> getNode(UUID id) {
		return session.optional(
			this::map,
			"SELECT * FROM PLAN_NODE WHERE NODE_ID = ?1",
			id
		);
	}


	@Override
	public boolean existsNode(UUID id) {
		return session.exists("select 1 from plan_node where node_id = ?1",id);
	}


	@Override
	public Optional<PlanNode> getParentNode(UUID id) {
		assertExistsNode(id);
		return session.optional(
			this::map,
			"SELECT PATH[1] FROM PLAN_NODE_HIERARCHY WHERE NODE_ID = ?1 AND CARDINALITY(PATH)>0",
			id
		);
	}


	@Override
	public List<PlanNode> getNodeChildren(UUID id) {
		assertExistsNode(id);
		return session.list(
			this::map,
			"""
			SELECT * FROM PLAN_NODE_HIERARCHY WHERE
			 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
			 AND CARDINALITY(PATH)>0 AND PATH[1] = ?1
			 ORDER BY SIBLING_ORDER
			"""
			,id
		);
	}


	@Override
	public Stream<PlanNode> getNodeDescendants(UUID id) {
		assertExistsNode(id);
		return session.stream(
			this::map,
			"""
			SELECT * FROM PLAN_NODE_HIERARCHY WHERE
			 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
			 AND '?1 IN (UNNEST(PATH))
			 ORDER BY CARDINALITY (PATH), SIBLING_ORDER
			""",
			id
		);
	}


	@Override
	public Stream<PlanNode> getNodeAncestors(UUID id) {
		assertExistsNode(id);
		return session.stream(
			this::map,
			"""
			SELECT C1
			 FROM PLAN_NODE_HIERARCHY H CROSS JOIN
			 UNNEST(H.PATH) WHERE H.NODE_ID = ?1
			 ORDER BY (SELECT CARDINALITY(PATH) FROM PLAN_NODE_HIERARCHY HH WHERE HH.NODE_ID = C1) DESC
			""",
			id
		);
	}


	@Override
	public UUID persistNode(PlanNode node) {
		return null;
	}

	@Override
	public void deleteNode(UUID id) {
		getParentNode(id).ifPresent(parent -> detachChildNode(parent.nodeID(), id));
		session.execute("DELETE from plan_node where node_id = ?1", id);
	}



	@Override
	public void attachChildNode(UUID parent, UUID child) {
		attachChildNode(parent,child,false);
	}


	@Override
	public void attachChildNodeFirst(UUID parent, UUID child) {
		attachChildNode(parent,child,true);
	}


	private void attachChildNode(UUID parent, UUID child, boolean first) {
		assertExistsNode(parent);
		assertExistsNode(child);
		// update path of child descendants
		session.execute("""
			UPDATE PLAN_NODE_HIERARCHY SET
			 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1),
			 PATH = PATH || (SELECT PATH FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
			 WHERE
			 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?2)
			 AND ?2 IN (UNNEST(PATH))
			""",
			parent,
			child
		);

		if (first) {
			// update sibling order of the new siblings
			session.execute("""
				UPDATE PLAN_NODE_HIERARCHY
				 SET SIBLING_ORDER = SIBLING_ORDER + 1
				 WHERE
				 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
				 AND CARDINALITY(PATH)>0 AND PATH[1] = ?1
				""",
				parent
			);
			// update root, path and sibling order of the attached node
			session.execute("""
				UPDATE PLAN_NODE_HIERARCHY SET
				 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1),
				 PATH = (SELECT PATH FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1),
				 SIBLING_ORDER = 0
				 WHERE NODE_ID = ?2
				""",
				parent,
				child
			);

		} else {
			// update root, path and sibling order of the attached node
			session.execute("""
				UPDATE PLAN_NODE_HIERARCHY SET
				 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1),
				 PATH = (SELECT PATH FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1),
				 SIBLING_ORDER = (
				  SELECT COUNT(*)+1 FROM PLAN_NODE_HIERARCHY H WHERE
				  H.ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY HH WHERE HH.NODE_ID = ?1)
				  AND CARDINALITY(H.PATH)>0 AND H.PATH[1] = ?1
				 )
				 WHERE NODE_ID = ?2
				""",
				parent,
				child
			);
		}
	}





	@Override
	public void detachChildNode(UUID parent, UUID child) {
		assertExistsNode(parent);
		assertExistsNode(child);
		// update sibling_order of siblings
		session.execute("""
			UPDATE PLAN_NODE_HIERARCHY SET SIBLING_ORDER = SIBLING_ORDER-1
			 WHERE
			 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
			 AND PATH = (SELECT PATH FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1) AND NODE_ID <> ?1
			 AND SIBLING_ORDER > (SELECT SIBLING_ORDER FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
			""",
			child
		);
		// update path of descendants of the detached node
		session.execute("""
			UPDATE PLAN_NODE_HIERARCHY SET
			 ROOT = (SELECT root FROM PLAN_NODE_HIERARCHY h WHERE h.NODE_ID = ?1)
			 PATH = TRIM_ARRAY(PATH,(SELECT CARDINALITY(PATH) FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1))
			 WHERE
			 ROOT = ?1
			 AND ?1 IN (UNNEST(PATH))
			""",
		    child
		);
		// update path and root of the detached node
		session.execute("""
			UPDATE PLAN_NODE_HIERARCHY SET
			 root = ?1,
			 PATH = array[],
			 SIBLING_ORDER = 0
			 WHERE NODE_ID = ?1
			""",
			child
		);
	}







	@Override
	public Stream<PlanNode> searchNodes(PlanNodeCriteria criteria) {
		return null;
	}



	private UUID rootPlanNode(UUID id) {
		return session.single(
			this::uuid,
			"SELECT ROOT from plan_node_hierarchy where node_id = ?1",
			id
		);
	}

	private UUID uuid(ResultSet resultSet) throws SQLException {
		return resultSet.getObject(1,UUID.class);
	}


	private PlanNode map(ResultSet resultSet) throws SQLException {
		return new PlanNode().nodeID(resultSet.getObject("ID",UUID.class));
	}


	private void assertExistsNode(UUID id) {
		if (!existsNode(id)) {
			throw new WakamitiException("Plan node {id} not present in repository", id);
		}
	}


}
