package es.iti.wakamiti.plugins.gherkin.parser;

import java.util.*;
import java.util.stream.*;

public class GherkinDialect {


    private final Locale locale;
    private final Map<KeywordType, List<String>> flattenedKeywordMap;


    GherkinDialect(Locale locale, KeywordMap keywordMap) {
        this.locale = locale;
        this.flattenedKeywordMap = Stream.of(KeywordType.values())
            .collect(Collectors.toMap(it -> it, it -> computeKeywords(keywordMap, it)));
    }


    public String language() {
        return locale.toLanguageTag();
    }


    public List<String> keywords(KeywordType type) {
        return flattenedKeywordMap.getOrDefault(type, List.of());
    }


    private static List<String> computeKeywords(KeywordMap keywordMap, KeywordType keywordType) {
        return keywordType.flattened()
            .map(keywordMap::keywords)
            .flatMap(Collection::stream)
            .toList();
    }


}
