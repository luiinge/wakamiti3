import es.iti.wakamiti.api.contributors.*;
import es.iti.wakamiti.plugins.gherkin.GherkinResourceProvider;

module es.iti.wakamiti.plugins.gherkin {

	requires static lombok;
	requires transitive es.iti.wakamiti.api;

	//requires transitive es.iti.wakamiti.core;

	exports es.iti.wakamiti.plugins.gherkin;
	exports es.iti.wakamiti.plugins.gherkin.parser;

	opens es.iti.wakamiti.plugins.gherkin to jexten;

	provides ConfigProvider with es.iti.wakamiti.plugins.gherkin.GherkinConfig;
	provides PlanProvider with es.iti.wakamiti.plugins.gherkin.GherkinPlanProvider;
	provides ResourceProvider with GherkinResourceProvider;

}