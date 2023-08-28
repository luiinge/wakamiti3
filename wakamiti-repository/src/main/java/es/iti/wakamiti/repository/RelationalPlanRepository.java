package es.iti.wakamiti.repository;


import com.github.f4b6a3.ulid.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.repository.*;

public class RelationalPlanRepository implements PlanRepository, AutoCloseable {


	private static final Log log = Log.of();

	private static class UlidFactoryHolder {
		static final UlidFactory INSTANCE = UlidFactory.newInstance();
	}


	private static String selectPlanNodeIn(String sql) {
		return "SELECT * FROM V_PLAN_NODE WHERE NODE_ID IN ("+sql+")";
	}

	private static final String SQL_SELECT_ROOT_NODE_ID = """
		SELECT ROOT FROM PLAN_NODE_HIERARCHY WHERE NODE_ID = ?1
		""";

	private static final String SQL_SELECT_PARENT_NODE_ID = """
		SELECT PATH[1] FROM PLAN_NODE_HIERARCHY WHERE NODE_ID = ?1 AND CARDINALITY(PATH)>0
		""";

	private static final String SQL_SELECT_CHILDREN_NODE_ID = """
		SELECT NODE_ID FROM PLAN_NODE_HIERARCHY WHERE
		 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
		 AND (CASE WHEN CARDINALITY(PATH)>0 THEN PATH[1] = ?1 ELSE FALSE END)
		 ORDER BY SIBLING_ORDER
		""";

	private static final String SQL_SELECT_DESCENDANT_NODE_ID = """
		SELECT NODE_ID FROM PLAN_NODE_HIERARCHY WHERE
		 ROOT = (SELECT ROOT FROM PLAN_NODE_HIERARCHY H WHERE H.NODE_ID = ?1)
		 AND ARRAY_CONTAINS(PATH,?1)
		 ORDER BY CARDINALITY (PATH), SIBLING_ORDER
		""";

	private static final String SQL_SELECT_ANCESTOR_NODE_ID = """
		SELECT C1
		 FROM PLAN_NODE_HIERARCHY H CROSS JOIN
		 UNNEST(SELECT hh.PATH FROM PLAN_NODE_HIERARCHY hh WHERE hh.NODE_ID = ?1)
		 WHERE H.NODE_ID = ?1
		 ORDER BY (SELECT CARDINALITY(PATH) FROM PLAN_NODE_HIERARCHY HH WHERE HH.NODE_ID = C1) DESC
		""";

	private final Session session;


	public RelationalPlanRepository(RepositoryServer server) {
		this.session = new Session(server::newConnection,log);
	}


	@Override
	public void close() throws SQLException {
		this.session.close();
	}


	@Override
	public void commit() {
		this.session.commit();
	}


	@Override
	public Optional<PlanNode> getNode(UUID id) {
		return session.optional(
			this::mapPlanNode,
			"SELECT * FROM V_PLAN_NODE WHERE NODE_ID = ?1",
			id
		);
	}



	@Override
	public boolean existsNode(UUID id) {
		return session.exists("SELECT 1 FROM PLAN_NODE WHERE NODE_ID = ?1",id);
	}


	@Override
	public Optional<UUID> getParentNodeID(UUID id) {
		assertExistsNode(id);
		return session.optional(this::uuid,	SQL_SELECT_PARENT_NODE_ID, id);
	}


	@Override
	public Optional<PlanNode> getParentNode(UUID id) {
		assertExistsNode(id);
		return session.optional(this::mapPlanNode, selectPlanNodeIn(SQL_SELECT_PARENT_NODE_ID), id);
	}


	@Override
	public Optional<UUID> getRootNodeID(UUID id) {
		assertExistsNode(id);
		return session.optional(this::uuid, SQL_SELECT_ROOT_NODE_ID, id);
	}


	@Override
	public Optional<PlanNode> getRootNode(UUID id) {
		assertExistsNode(id);
		return session.optional(this::mapPlanNode, selectPlanNodeIn(SQL_SELECT_ROOT_NODE_ID), id);
	}


	@Override
	public List<UUID> getNodeChildrenID(UUID id) {
		assertExistsNode(id);
		return session.list(this::uuid, SQL_SELECT_CHILDREN_NODE_ID, id);
	}


	@Override
	public List<PlanNode> getNodeChildren(UUID id) {
		assertExistsNode(id);
		return session.list(this::mapPlanNode, selectPlanNodeIn(SQL_SELECT_CHILDREN_NODE_ID), id);
	}


	@Override
	public Stream<UUID> getNodeDescendantsID(UUID id) {
		assertExistsNode(id);
		return session.stream(this::uuid, SQL_SELECT_DESCENDANT_NODE_ID, id);
	}


	@Override
	public Stream<PlanNode> getNodeDescendants(UUID id) {
		assertExistsNode(id);
		return session.stream(this::mapPlanNode, selectPlanNodeIn(SQL_SELECT_DESCENDANT_NODE_ID), id);
	}


	@Override
	public Stream<UUID> getNodeAncestorsID(UUID id) {
		assertExistsNode(id);
		return session.stream(this::uuid, SQL_SELECT_ANCESTOR_NODE_ID, id);
	}


	@Override
	public Stream<PlanNode> getNodeAncestors(UUID id) {
		assertExistsNode(id);
		return session.stream(this::mapPlanNode, selectPlanNodeIn(SQL_SELECT_ANCESTOR_NODE_ID), id);
	}


	@Override
	public UUID persistNode(PlanNode node) {

		String sql;
		UUID id;
		boolean update = (node.nodeID() != null);

		// node
		if (update) {
			id = node.nodeID();
			assertExistsNode(id);
			sql = """
				UPDATE PLAN_NODE SET
				 TYPE = ?2,
				 NAME = ?3,
				 LANGUAGE = ?4,
				 IDENTIFIER = ?5,
				 SOURCE = ?6,
				 KEYWORD = ?7,
				 DESCRIPTION = ?8,
				 DISPLAY_NAME_PATTERN = ?9,
				 DATA_TABLE = ?10,
				 DOCUMENT = ?11,
				 DOCUMENT_TYPE = ?12
				 WHERE NODE_ID = ?1
				""";
		} else {
			id = generateUUID();
			node.nodeID(id);
			sql = """
				INSERT INTO PLAN_NODE (
				 NODE_ID,
				 TYPE,
				 NAME,
				 LANGUAGE,
				 IDENTIFIER,
				 SOURCE,
				 KEYWORD,
				 DESCRIPTION,
				 DISPLAY_NAME_PATTERN,
				 DATA_TABLE,
				 DOCUMENT,
				 DOCUMENT_TYPE
				) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12)
				""";
		}
		session.execute(
			sql,
			id,
			node.nodeType() == null ? null : node.nodeType().numericValue,
			node.name(),
			node.language(),
			node.identifier(),
			node.source(),
			node.keyword(),
			node.description(),
			node.displayNamePattern(),
			node.dataTable() == null ? null : node.dataTable().toString(),
			node.document() == null ? null : node.document().content(),
			node.document() == null ? null : node.document().contentType()
		);

		if (!update) {
			// hierarchy
			session.execute(
				"INSERT INTO PLAN_NODE_HIERARCHY (NODE_ID,ROOT,PATH,SIBLING_ORDER) VALUES (?1,?1,ARRAY[],1)",
				id
			);
		}

		// tags
		session.execute("DELETE FROM PLAN_NODE_TAG WHERE NODE_ID = ?1",id);
		if (!node.tags().isEmpty()) {
			List<List<?>> args = node.tags().stream()
				.map(tag -> List.of(id, tag))
				.collect(Collectors.toList());
			session.executeBatch("INSERT INTO PLAN_NODE_TAG (NODE_ID,TAG) VALUES (?1,?2)",args);
		}
		// properties
		session.execute("DELETE FROM PLAN_NODE_PROPERTY WHERE NODE_ID = ?1",id);
		if (!node.properties().isEmpty()) {
			List<List<?>> args = node.properties().entrySet().stream().map(
				property -> List.of(id, property.getKey(), property.getValue())
			).collect(Collectors.toList());
			session.executeBatch(
				"INSERT INTO PLAN_NODE_PROPERTY (NODE_ID,PROPERTY_KEY,PROPERTY_VALUE) VALUES (?1,?2,?3)",
				args
			);
		}



		return id;

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
			 AND ARRAY_CONTAINS(PATH,?2)
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
				  AND CASE WHEN CARDINALITY(H.PATH)>0 THEN H.PATH[1] = ?1 ELSE FALSE END
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
			 AND ARRAY_CONTAINS(PATH, ?1)
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



	private PlanNode mapPlanNode(ResultSet resultSet) throws SQLException {
		return new PlanNode()
			.nodeID(resultSet.getObject("NODE_ID",UUID.class))
			.nodeType(NodeType.of(resultSet.getInt("TYPE")))
			.dataTable(DataTable.fromString(resultSet.getString("DATA_TABLE")))
			.document(Document.of(resultSet.getString("DOCUMENT_TYPE"),resultSet.getString("DOCUMENT")))
			.language(resultSet.getString("LANGUAGE"))
			.source(resultSet.getString("SOURCE"))
			.name(resultSet.getString("NAME"))
			.description(resultSet.getString("DESCRIPTION"))
			.displayNamePattern(resultSet.getString("DISPLAY_NAME_PATTERN"))
			.keyword(resultSet.getString("KEYWORD"))
			.identifier(resultSet.getString("IDENTIFIER"))
			.tags(arrayAsSet(resultSet.getArray("TAGS")))
			.properties(arrayAsSortedMap(resultSet.getArray("PROPERTIES"),"="))
		;
	}



	private Set<String> arrayAsSet(Array array) throws SQLException {
		var set = new HashSet<String>();
		if (array != null && array.getArray() != null) {
			Arrays.stream(((Object[]) array.getArray())).map(Object::toString).forEach(set::add);
		}
		return set;
	}


	private SortedMap<String, String> arrayAsSortedMap(Array array, String separator)
	throws SQLException {
		var map = new TreeMap<String,String>();
		if (array != null && array.getArray() != null) {
			Stream.of((Object[])array.getArray())
				.map(Object::toString)
				.map(property->property.split(separator))
				.forEach(keyValue -> map.put(keyValue[0],keyValue[1]));
		}
		return map;
	}


	private void assertExistsNode(UUID id) {
		if (!existsNode(id)) {
			throw new WakamitiException("Plan node {id} not present in repository", id);
		}
	}


	private UUID generateUUID() {
		return UlidCreator.getUlid().toUuid();
	}

}
