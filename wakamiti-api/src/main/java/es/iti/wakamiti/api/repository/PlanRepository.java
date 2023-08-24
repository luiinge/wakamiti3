package es.iti.wakamiti.api.repository;

import java.util.*;
import java.util.stream.Stream;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.contributors.Contributor;
import es.iti.wakamiti.api.plan.PlanNode;
import jexten.ExtensionPoint;

@ExtensionPoint(version = "3.0")
public interface PlanRepository extends Contributor {


	Optional<PlanNode> getNode(UUID id);
	Optional<UUID> getParentNode(UUID id);


	/**
	 * Delete completely a plan node, including its child nodes.
	 * If the node was a child of another node, it will be detached.
	 * @throws WakamitiException if the id does not exist in the repository
	 */
	void deleteNode(UUID id);


	/**
	 * Attach a plan node as child of another node, at the end of the existing child list.
	 * If the child node was already in the child list, this operation will have no effect.
     * @throws WakamitiException if either the parent id or the child id do not exist in the repository
	 */
	void attachChildNode(UUID parent, UUID child);

	/**
	 * Attach a plan node as child of another node, at the beginning of the existing child list.
	 * If the child node was already in the child list, this operation will have no effect.
	 * @throws WakamitiException if either the parent id or the child id do not exist in the repository
	 */
	void attachChildNodeFirst(UUID parent, UUID child);


	/**
	 * Detach a plan node as a child of another node, keeping it in the repository as an orphan node.
	 * If the child node was not already in the child list, this operation will have no effect.
	 * @throws WakamitiException if either the parent id or the child id do not exist in the repository
	 */
	void detachChildNode(UUID parent, UUID child);


	/**
	 * Persist a plan node in the repository. If the node id did exist previously, it
	 * will update the node content; otherwise, it will create a new record and assign a
	 * unique id.
	 * @return The assigned node id
	 */
	UUID persistNode(PlanNode node);

	Stream<PlanNode> searchNodes(PlanNodeCriteria criteria);




}
