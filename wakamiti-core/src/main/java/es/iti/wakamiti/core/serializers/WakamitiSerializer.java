package es.iti.wakamiti.core.serializers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.contributors.*;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.repository.PlanRepository;
import es.iti.wakamiti.core.WakamitiMapper;
import java.io.*;
import jexten.*;

@Extension
public class WakamitiSerializer implements PlanSerializer  {


	private final ObjectMapper mapper = new WakamitiMapper()
		.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


	@Inject
	PlanRepository repository;

	@Override
	public void serializeTree(PlanNodeID planNodeID, Writer writer) {
		try {
			serialize(planNodeID,writer,1);
		} catch (IOException e) {
			throw new WakamitiException(e,"Cannot serialize plan node {id}",planNodeID);
		}
	}



	private void serialize(PlanNodeID nodeID, Writer writer, int level) throws IOException {
		PlanNode node = repository.get(nodeID).orElseThrow();
		node.nodeID(null);

		var children = repository.getChildren(nodeID);
		String margin1 = "  ".repeat(level-1);
		String margin2 = "  ".repeat(level);
		String nodeJson = mapper.writeValueAsString(node);
		nodeJson = nodeJson.substring(2,nodeJson.length()-2);
		nodeJson = margin2+nodeJson.replace("\n","\n"+margin2);

		writer.append(margin1).append("{\n");
		writer.append(margin2).append("\"node\" : {\n");
		writer.append(nodeJson).append("\n");
		writer.append(margin2).append(children.isEmpty() ? "}\n" : "},\n");

		if (!children.isEmpty()) {
			writer.append(margin2).append("\"children\" : [\n");
			for (int i = 0; i < children.size(); i++) {
				serialize(children.get(i), writer, level + 2);
				if (i < children.size() - 1) {
					writer.append(",\n");
				}
			}
			writer.append("\n").append(margin2).append("]\n");
		}
		writer.append(margin1).append("}");

	}

}
