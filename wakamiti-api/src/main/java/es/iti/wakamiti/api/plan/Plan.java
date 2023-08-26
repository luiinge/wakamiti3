package es.iti.wakamiti.api.plan;

import java.util.UUID;

public record Plan(
	UUID planID,
	String organization,
	String project,
	String name,
	String hash,
	String tagFilter,
	UUID rootNode
) {

}
