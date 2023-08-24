import es.iti.wakamiti.api.Wakamiti;
import es.iti.wakamiti.api.contributors.*;
import es.iti.wakamiti.api.repository.PlanRepository;
import es.iti.wakamiti.core.*;
import es.iti.wakamiti.core.repository.NitriteRepository;
import es.iti.wakamiti.core.serializers.*;

module es.iti.wakamiti.core {

	requires transitive es.iti.wakamiti.api;

	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires nitrite;

	exports es.iti.wakamiti.core;
	exports es.iti.wakamiti.core.datatypes;
	exports es.iti.wakamiti.core.contenttypes;
	exports es.iti.wakamiti.core.util;
	exports es.iti.wakamiti.core.repository;
	exports es.iti.wakamiti.core.serializers;

	opens es.iti.wakamiti.core to jexten;
	opens es.iti.wakamiti.core.repository to jexten;
	opens es.iti.wakamiti.core.serializers to jexten;


	provides DataTypeProvider with BasicDataTypes;
	provides ContentTypeProvider with BasicContentTypes;
	provides PlanRepository with NitriteRepository;
	provides ConfigProvider with WakamitiProperties;
	provides PlanSerializer with WakamitiSerializer, SimpleTreeSerializer;
	provides Wakamiti.Builder with WakamitiEngineBuilder;

}