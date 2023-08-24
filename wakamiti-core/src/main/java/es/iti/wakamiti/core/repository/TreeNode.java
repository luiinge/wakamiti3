package es.iti.wakamiti.core.repository;

import java.util.*;

import org.dizitart.no2.objects.Id;

import es.iti.wakamiti.api.plan.PlanNodeID;

public class TreeNode {

	@Id
	public PlanNodeID nodeID;

	public PlanNodeID parentID;

	public List<PlanNodeID> children = new ArrayList<>();


	public TreeNode() {

	}

	public TreeNode(PlanNodeID nodeID) {
		this.nodeID = nodeID;
	}


}
