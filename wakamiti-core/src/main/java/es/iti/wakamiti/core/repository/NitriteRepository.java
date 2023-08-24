package es.iti.wakamiti.core.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.iti.wakamiti.api.repository.*;
import static org.dizitart.no2.objects.filters.ObjectFilters.*;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.dizitart.no2.*;
import org.dizitart.no2.Document;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.mapper.*;
import org.dizitart.no2.objects.*;
import org.dizitart.no2.objects.filters.ObjectFilters;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.lang.Lazy;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.resources.BuildPlanRequest;
import es.iti.wakamiti.core.*;
import imconfig.Config;
import jexten.*;

@Extension(scope = Scope.GLOBAL)
public class NitriteRepository implements PlanRepository {

	private static final Log log = Log.of();

	private final JacksonMapper mapper = new JacksonMapper(new JacksonFacade() {
		@Override
		protected ObjectMapper createObjectMapper() {
			return new WakamitiMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
		}
	});




	private final Lazy<Nitrite> db;
	private final Lazy<ObjectRepository<PlanNode>> nodeRepository;
	private final Lazy<ObjectRepository<TreeNode>> nodeTreeRepository;
	private final Lazy<NitriteCollection> planRequestCache;
	private final Document nodeIDProjection;

	@Inject
	Config config;

	@Inject
	IDProvider idProvider;



	public NitriteRepository() {
		this.db = Lazy.of(this::createDatabase);
		this.nodeRepository = Lazy.of(()-> repository(db.get(), "nodes", PlanNode.class, "nodeID"));
		this.nodeTreeRepository = Lazy.of(()->db.get().getRepository("treeNodes", TreeNode.class));
		this.planRequestCache = Lazy.of(()->collection(db.get(),"planRequestCache","hash"));
		this.nodeIDProjection = Document.createDocument("nodeID",null);
	}


	protected Nitrite createDatabase() {
		var builder = Nitrite.builder().nitriteMapper(mapper);
		if (!config.getBoolean(WakamitiProperties.REPOSITORY_TRANSIENT,false)) {
			var path = config.get(WakamitiProperties.REPOSITORY_PATH, Path::of,Path.of("."))
				.toAbsolutePath()
				.resolve("nitrite")
				.toString();
			log.debug("using repository located in {path}",path);
			builder.filePath(path).compressed();
		} else {
			log.debug("using in-memory repository");
		}
		return builder.openOrCreate();
	}


	@Override
	public PlanNodeID persist(PlanNode node) {
		if (node.nodeID() == null) {
			node.nodeID(idProvider.newPlanNodeID());
			log.debug("creating new plan node {}",node.nodeID());
			nodeRepository.get().insert(node);
		} else {
			log.debug("updating plan node {}",node.nodeID());
			nodeRepository.get().update(byNodeID(node.nodeID()), node);
		}
		return node.nodeID();
	}


	@Override
	public Optional<PlanNodeID> getByPlanRequest(BuildPlanRequest buildPlanRequest) {
		var hash = buildPlanRequest.hash();
		return Optional.ofNullable(
			planRequestCache.get().find(Filters.eq("hash",hash)).firstOrDefault()
		).map( document -> document.get("nodeID",PlanNodeID.class));
	}


	@Override
	public void updateResourceSet(BuildPlanRequest buildPlanRequest, PlanNodeID planNodeID) {
		var hash = buildPlanRequest.hash();
		var document = planRequestCache.get().find(Filters.eq("hash", hash)).firstOrDefault();
		if (document == null) {
			document = new Document();
			document.put("hash", hash);
			document.put("nodeID", planNodeID);
			planRequestCache.get().insert(document);
		} else {
			document.put("nodeID", planNodeID);
			planRequestCache.get().update(document);
		}
	}


	@Override
	public boolean exists(PlanNodeID id) {
		return nodeRepository.get().find(byNodeID(id)).totalCount() > 0;
	}


	@Override
	public Optional<PlanNode> get(PlanNodeID id) {
		return first(nodeRepository,byNodeID(id));
	}


	@Override
	public Optional<PlanNodeID> getParent(PlanNodeID nodeID) {
		return first(nodeTreeRepository, byNodeID(nodeID)).map(it->it.parentID);
	}


	@Override
	public List<PlanNodeID> getAncestors(PlanNodeID nodeID) {
		return getAncestors(nodeID, -1);
	}

	@Override
	public List<PlanNodeID> getAncestors(PlanNodeID nodeID, int maxDepth) {
		return fillAncestors(nodeID, 0, maxDepth, new LinkedList<>());
	}

	@Override
	public List<PlanNodeID> findAncestors(PlanNodeID nodeID, PlanNodeCriteria criteria) {
		return findAncestors(nodeID,criteria,-1);
	}


	@Override
	public List<PlanNodeID> findAncestors(PlanNodeID nodeID, PlanNodeCriteria criteria, int maxDepth) {
		return filter(getAncestors(nodeID,maxDepth),criteria);
	}


	private List<PlanNodeID> fillAncestors(
		PlanNodeID nodeID,
		int depth,
		int maxDepth,
		List<PlanNodeID> results
	) {
		first(nodeTreeRepository, byNodeID(nodeID)).map(it -> it.parentID).ifPresent(it -> {
			results.add(it);
			if (maxDepth == -1 || depth < maxDepth) {
				fillAncestors(it, depth+1, maxDepth, results);
			}
		});
		return results;
	}

	@Override
	public List<PlanNodeID> getChildren(PlanNodeID nodeID) {
		return first(nodeTreeRepository,byNodeID(nodeID)).map(it -> it.children).orElseGet(List::of);
	}


	@Override
	public List<PlanNodeID> getDescendants(PlanNodeID nodeID) {
		return getDescendants(nodeID,-1);
	}

	@Override
	public List<PlanNodeID> getDescendants(PlanNodeID nodeID, int maxDepth) {
		return fillDescendants(nodeID,0,maxDepth,new LinkedList<>());
	}

	@Override
	public List<PlanNodeID> findDescendants(PlanNodeID nodeID, PlanNodeCriteria criteria) {
		return findDescendants(nodeID,criteria,-1);
	}

	@Override
	public List<PlanNodeID> findDescendants(PlanNodeID nodeID, PlanNodeCriteria criteria, int maxDepth) {
		return filter(getDescendants(nodeID,maxDepth),criteria);
	}


	private List<PlanNodeID> fillDescendants(
		PlanNodeID nodeID,
		int depth,
		int maxDepth,
		List<PlanNodeID> results
		) {
		results.addAll(getChildren(nodeID));
		if (maxDepth == -1 || depth < maxDepth) {
			getChildren(nodeID).forEach(child -> fillDescendants(child,depth+1,maxDepth,results));
		}
		return results;
	}


	@Override
	public void delete(PlanNodeID id) {
		log.debug("deleting plan node {}",id);
		if (!exists(id)) {
			throw new WakamitiException("Cannot delete node {id}",id);
		}
		ObjectFilter byID = byNodeID(id);
		List<PlanNodeID> descendants = getDescendants(id);
		log.debug("deleting also descendants: {}",descendants);
		Optional<PlanNodeID> parent = getParent(id);
		descendants.forEach(it -> nodeTreeRepository.get().remove(byNodeID(it)));
		if (parent.isPresent()) {
			var parentTree = nodeTreeRepository.get().find(byNodeID(parent.get())).firstOrDefault();
			if (parentTree == null) {
				throw new WakamitiException("Cannot delete node {id}",id);
			}
			parentTree.children.remove(id);
			nodeTreeRepository.get().update(parentTree);
		}
		nodeTreeRepository.get().remove(byID);
		nodeRepository.get().remove(byID);
		descendants.forEach(it -> nodeRepository.get().remove(byNodeID(it)));
	}




	@Override
	public void attachChild(PlanNodeID parent, PlanNodeID child) {
		attachChild(parent, child, -1);
	}


	@Override
	public void attachChild(PlanNodeID parent, PlanNodeID child, int index) {
		log.debug("attaching plan node {} as child of {}",child,parent);
		if (!exists(parent) || !exists(child)) {
			throw new WakamitiException("Cannot add child {} to node {}",child,parent);
		}
		var parentTree = firstOrNew(nodeTreeRepository, byNodeID(parent), ()->new TreeNode(parent));
		parentTree.children.remove(child);
		if (index < 0) {
			parentTree.children.add(child);
		} else {
			parentTree.children.add(index,child);
		}
		nodeTreeRepository.get().update(parentTree);
		var nodeTree = firstOrNew(nodeTreeRepository, byNodeID(child), ()->new TreeNode(child));
		nodeTree.parentID = parent;
		nodeTreeRepository.get().update(nodeTree);
	}


	@Override
	public void detachChild(PlanNodeID parent, PlanNodeID child) {
		log.debug("detaching plan node {} as child of {}",child,parent);
		if (!exists(parent) || !exists(child)) {
			throw new WakamitiException("Cannot detach node {id} from node {id}",child,parent);
		}
		var parentTree = first(nodeTreeRepository, byNodeID(parent));
		if (parentTree.isEmpty()) {
			throw new WakamitiException("Cannot detach node {id} from node {id}",child,parent);
		}
		parentTree.get().children.remove(child);
		nodeTreeRepository.get().update(parentTree.get());
	}




	private static ObjectFilter mapCriteria(PlanNodeCriteria criteria) {
		if (criteria instanceof PlanNodeCriteria.HasPropertyCriteria c)
			return eq("properties."+c.property(),c.value());
		if (criteria instanceof PlanNodeCriteria.HasTagCriteria c)
			return ObjectFilters.elemMatch("tags", eq("$",c.tag()));
		if (criteria instanceof PlanNodeCriteria.HasField c)
			return eq(c.field(),c.value());
		if (criteria instanceof PlanNodeCriteria.HasValuedField c)
			return not(eq(c.field(),null));
		if (criteria instanceof PlanNodeCriteria.HasNodeTypeCriteria c)
			return eq("nodeType",c.nodeType());
		if (criteria instanceof PlanNodeCriteria.AndCriteria c)
			return and(Stream.of(c.conditions()).map(NitriteRepository::mapCriteria).toArray(ObjectFilter[]::new));
		if (criteria instanceof PlanNodeCriteria.OrCriteria c)
			return ObjectFilters.or(Stream.of(c.conditions()).map(NitriteRepository::mapCriteria).toArray(ObjectFilter[]::new));
		if (criteria instanceof PlanNodeCriteria.NotCriteria c)
			return ObjectFilters.not(mapCriteria(c.condition()));
		throw new WakamitiException("Unexpected repository criteria");
	}



	private static ObjectFilter byNodeID(PlanNodeID id) {
		return eq("nodeID",id);
	}


	private <T> Optional<T> first(Lazy<ObjectRepository<T>> repository, ObjectFilter filter) {
		return Optional.ofNullable(repository.get().find(filter).firstOrDefault());
	}


	private <T> T firstOrNew(Lazy<ObjectRepository<T>> repository, ObjectFilter filter, Supplier<T> supplier) {
		T first = repository.get().find(filter).firstOrDefault();
		if (first == null) {
			first = supplier.get();
			repository.get().insert(first);
		}
		return first;
	}


	private List<PlanNodeID> filter(List<PlanNodeID> ids, PlanNodeCriteria criteria) {
		if (ids.isEmpty()) {
			return List.of();
		}
		List<PlanNodeID> result = new LinkedList<>();
		var filter = and(
			in("nodeID",ids.toArray()),
			mapCriteria(criteria)
		);
		filter.setNitriteMapper(mapper);
		nodeRepository.get().getDocumentCollection().find(filter)
			.project(nodeIDProjection)
			.forEach(document -> result.add(new PlanNodeID(document.get("nodeID",String.class))));
		return result;
	}



	public void checkIntegrity() {
		nodeTreeRepository.get().find(ObjectFilters.ALL).forEach(tree -> {
			String document = "{nodeID:%s | parent:%s | children:%s}".formatted(
				tree.nodeID.shortValue(),
				tree.parentID == null ? null : tree.parentID.shortValue(),
				tree.children.stream().map(PlanNodeID::shortValue).toList()
			);
			System.out.println(document);
			if (!exists(tree.nodeID)) {
				throw new WakamitiException("{} in tree but not in node repository",tree.nodeID.shortValue());
			}
			if (tree.parentID != null && !exists(tree.parentID)) {
				throw new WakamitiException("{} in tree as parent of {} but not in node repository",
					tree.parentID.shortValue(),
					tree.nodeID.shortValue()
				);
			}
			if (tree.parentID != null && first(nodeTreeRepository,byNodeID(tree.parentID)).isEmpty()) {
				throw new WakamitiException("{} in tree as parent of {} but has no own node in tree",
					tree.parentID.shortValue(),
					tree.nodeID.shortValue()
				);
			}

			for (var child: tree.children) {
				if (!exists(child)) {
					throw new WakamitiException("{} in tree as child of {} but not in node repository",
						child.shortValue(),
						tree.nodeID.shortValue()
					);
				}
				if (first(nodeTreeRepository,byNodeID(child)).isEmpty()) {
					throw new WakamitiException("{} in tree as child of {} but has no own node in tree",
						child.shortValue(),
						tree.nodeID.shortValue()
					);
				}
			}
		});
	}


	private static <T> ObjectRepository<T> repository(Nitrite db, String name, Class<T> type, String id) {
		var repo = db.getRepository(name,type);
		if (!repo.hasIndex(id)) {
			repo.createIndex(id, IndexOptions.indexOptions(IndexType.Unique));
		}
		return repo;
	}


	private static NitriteCollection collection(Nitrite db, String name, String id) {
		var collection = db.getCollection(name);
		if (!collection.hasIndex(id)) {
			collection.createIndex(id, IndexOptions.indexOptions(IndexType.Unique));
		}
		return collection;
	}




}
