package es.iti.wakamiti.plugins.gherkin.parser;

import java.util.*;

import es.iti.wakamiti.plugins.gherkin.parser.elements.Location;
import es.iti.wakamiti.plugins.gherkin.parser.internal.NoSuchLanguageException;


public class GherkinDialectFactory {

    private static final Map<Locale, GherkinDialect> dialectCache = new HashMap<>();

    private final KeywordMapProvider keywordMapProvider;
    private final GherkinDialect defaultDialect;


    public GherkinDialectFactory(KeywordMapProvider keywordMapProvider, String defaultDialectName) {
        this.keywordMapProvider = keywordMapProvider;
        this.defaultDialect = dialectFor(defaultDialectName);
    }


    public GherkinDialect dialectFor(String language) {
        return dialectFor(Locale.forLanguageTag(language));
    }


    public GherkinDialect dialectFor(Locale locale) {
        return dialectCache.computeIfAbsent(locale, this::readDialectFor);
    }


    private GherkinDialect readDialectFor(Locale locale) {
        return keywordMapProvider.keywordMap(locale)
            .map( it -> new GherkinDialect(locale,it))
            .orElseThrow(()-> new NoSuchLanguageException(locale, new Location()));
    }


    public GherkinDialect defaultDialect() {
        return this.defaultDialect;
    }




}
