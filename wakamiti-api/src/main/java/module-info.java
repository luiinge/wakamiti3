import es.iti.wakamiti.api.repository.PlanRepository;

module es.iti.wakamiti.api {

	// compile-only
	requires static lombok;
	// exposed api
	requires transitive jexten;
	requires transitive imconfig;
	// internal implementation
	requires slf4jansi;

	exports es.iti.wakamiti.api;
	exports es.iti.wakamiti.api.contributors;
	exports es.iti.wakamiti.api.lang;
	exports es.iti.wakamiti.api.plan;
	exports es.iti.wakamiti.api.resources;

	opens es.iti.wakamiti.api.contributors to jexten;
	opens es.iti.wakamiti.api to jexten;
	exports es.iti.wakamiti.api.repository;
	opens es.iti.wakamiti.api.repository to jexten;


	uses es.iti.wakamiti.api.contributors.DataTypeProvider;
	uses es.iti.wakamiti.api.contributors.ContentTypeProvider;
	uses es.iti.wakamiti.api.contributors.PlanProvider;
	uses es.iti.wakamiti.api.contributors.ResourceProvider;
	uses es.iti.wakamiti.api.contributors.ConfigProvider;
	uses PlanRepository;
	uses es.iti.wakamiti.api.contributors.PlanSerializer;
	uses es.iti.wakamiti.api.Wakamiti.Builder;


}