package es.iti.wakamiti.plugins.gherkin.parser;

import java.io.*;
import java.util.*;

import es.iti.wakamiti.plugins.gherkin.parser.elements.GherkinDocument;
import es.iti.wakamiti.plugins.gherkin.parser.internal.*;

public class GherkinParser {

    private final Parser parser;

    public GherkinParser(KeywordMapProvider keywordMapProvider) {
        this.parser = new Parser(keywordMapProvider);
    }

    public GherkinParser(List<KeywordMapProvider> keywordMapProviders) {
        this.parser = new Parser(new AggregateKeywordMapProvider(keywordMapProviders));
    }

    public GherkinParser(KeywordMap keywordMap) {
        this.parser = new Parser(it -> Optional.of(keywordMap));
    }

    public GherkinDocument parse(Reader reader) {
        return parser.parse(reader);
    }

    public GherkinDocument parse(InputStream inputStream) {
        return parser.parse(inputStream);
    }

}
