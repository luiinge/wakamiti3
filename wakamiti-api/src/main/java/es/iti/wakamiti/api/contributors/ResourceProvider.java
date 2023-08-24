package es.iti.wakamiti.api.contributors;

import java.util.List;

import es.iti.wakamiti.api.resources.Resource;
import jexten.ExtensionPoint;

@ExtensionPoint
public interface ResourceProvider {

	List<Resource> resources();
	
}
