package es.iti.wakamiti.api.contributors;

import java.util.stream.Stream;

import es.iti.wakamiti.api.ContentType;
import jexten.ExtensionPoint;

@ExtensionPoint
public interface ContentTypeProvider extends Contributor {

	Stream<ContentType> contentTypes();

}
