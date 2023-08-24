module es.iti.wakamiti.plugins.gherkin.test {
	requires es.iti.wakamiti.core;
	requires es.iti.wakamiti.plugins.gherkin;
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.params;
	opens es.iti.wakamiti.plugins.gherkin.test to org.junit.platform.commons;
}