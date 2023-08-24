package es.iti.wakamiti.plugins.gherkin.parser;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import es.iti.wakamiti.plugins.gherkin.DefaultKeywordMapProvider;

public class TestGherkinParser {

	@Test
	void parseGherkinDocument() {
		var keywordMapProvider = new DefaultKeywordMapProvider();
		var parser = new GherkinParser(keywordMapProvider);
		var parsed = parser.parse(getClass().getResourceAsStream("/simpleScenario.feature"));
		assertThat(parsed).isNotNull();
	}

	@Test
	void parseGherkinDocumentWithLanguage() {
		var keywordMapProvider = new DefaultKeywordMapProvider();
		var parser = new GherkinParser(keywordMapProvider);
		var parsed = parser.parse(getClass().getResourceAsStream("/implementation.feature"));
		assertThat(parsed).isNotNull();
	}

}
