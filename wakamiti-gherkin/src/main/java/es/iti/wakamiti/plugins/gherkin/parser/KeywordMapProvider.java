package es.iti.wakamiti.plugins.gherkin.parser;

import java.util.*;

@FunctionalInterface
public interface KeywordMapProvider {

    Optional<KeywordMap> keywordMap(Locale locale);

}
