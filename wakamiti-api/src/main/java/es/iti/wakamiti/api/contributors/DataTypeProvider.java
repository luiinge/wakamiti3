package es.iti.wakamiti.api.contributors;

import es.iti.wakamiti.api.DataType;
import java.util.stream.Stream;
import jexten.ExtensionPoint;

@ExtensionPoint
public interface DataTypeProvider extends Contributor {

	Stream<DataType> dataTypes();

}
