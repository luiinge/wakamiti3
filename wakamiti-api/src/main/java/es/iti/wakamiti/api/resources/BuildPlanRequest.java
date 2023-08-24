package es.iti.wakamiti.api.resources;

import es.iti.wakamiti.api.contributors.PlanProvider;
import java.util.*;

import es.iti.wakamiti.api.lang.Lazy;
import imconfig.Config;

public class BuildPlanRequest {

	private static final Comparator<Resource> resourceComparator =
		Comparator.comparing(Resource::URI);

	private final Config config;
	private final List<Resource> resources;
	private final Lazy<Hash> hash = Lazy.of(this::calculateHash);


	public BuildPlanRequest(Config config, List<Resource> resources) {
		this.config = config;
		this.resources = resources.stream().sorted(resourceComparator).toList();
	}


	public List<Resource> resources() {
		return resources;
	}


	public List<Resource> resourcesAcceptedBy(PlanProvider planProvider) {
		return resources.stream().filter(planProvider::accept).toList();
	}


	public Config config() {
		return config;
	}


	public Hash hash() {
		return hash.get();
	}


	private Hash calculateHash() {
		StringBuilder hash = new StringBuilder();
		hash.append(config.toString()).append("#");
		for (var resource : resources) {
			hash.append(resource.hash().value()).append("#");
		}
		return Hash.of(hash.toString());
	}

}
