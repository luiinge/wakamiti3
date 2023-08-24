package es.iti.wakamiti.core.serializers;

import es.iti.wakamiti.api.repository.PlanRepository;
import java.io.*;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.contributors.*;
import es.iti.wakamiti.api.plan.*;
import java.util.stream.Collectors;
import jexten.*;

@Extension(priority = Priority.LOWEST, name = "simple")
public class SimpleTreeSerializer implements PlanSerializer  {


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
		var children = repository.getChildren(nodeID);
		String margin = "  ".repeat(level);
		writer.append(margin).append(
			"- %s %s %s%s %s %n".formatted(
				node.nodeID().shortValue(),
				node.nodeType().symbol,
				node.id() == null ? "" : " ["+node.id()+"] ",
				node.displayName(),
				node.tags().stream().map(it -> "| #"+it).collect(Collectors.joining(""))
			));
		for (PlanNodeID child : children) {
			serialize(child, writer, level + 1);
		}
	}

}
