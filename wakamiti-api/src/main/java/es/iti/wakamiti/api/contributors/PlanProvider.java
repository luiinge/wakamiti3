package es.iti.wakamiti.api.contributors;

import java.util.*;

import es.iti.wakamiti.api.resources.Resource;
import jexten.ExtensionPoint;

@ExtensionPoint
public interface PlanProvider extends Contributor {

	boolean accept(Resource resource);

	Optional<UUID> providePlan(List<Resource> resources);

}
