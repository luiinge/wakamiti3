package es.iti.wakamiti.api.contributors;

import java.io.*;
import java.util.UUID;

import jexten.ExtensionPoint;

@ExtensionPoint
public interface PlanSerializer extends Contributor {

	void serializeTree(UUID planNodeID, Writer writer);

	default String serializeTreeToString(UUID planNodeID) {
		StringWriter writer = new StringWriter();
		writer.append("\n");
		serializeTree(planNodeID,writer);
		return writer.toString();
	}

}
