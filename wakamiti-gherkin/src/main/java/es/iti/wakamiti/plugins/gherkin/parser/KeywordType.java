package es.iti.wakamiti.plugins.gherkin.parser;

import java.util.stream.Stream;

public enum KeywordType {
    FEATURE("feature"),
    BACKGROUND("background"),
    SCENARIO("scenario"),
    SCENARIO_OUTLINE("scenarioOutline"),
    EXAMPLES("examples"),
    GIVEN("given"),
    WHEN("when"),
    THEN("then"),
    AND("and"),
    BUT("but"),
    STEP(GIVEN, WHEN, THEN, AND, BUT),
    SCENARIO_DEFINITION(BACKGROUND, SCENARIO, SCENARIO_OUTLINE),
    ALL(FEATURE, SCENARIO_DEFINITION, STEP, EXAMPLES);

    private final String key;
    private final KeywordType[] includedTypes;


    KeywordType(KeywordType... includedTypes) {
        this.key = name();
        this.includedTypes = includedTypes;
    }


    KeywordType(String key) {
        this.key = key;
        this.includedTypes = new KeywordType[0];
    }


    public String key() {
        return key;
    }


    Stream<KeywordType> flattened() {
        return includedTypes.length == 0 ?
            Stream.of(this) :
            Stream.of(includedTypes).flatMap(KeywordType::flattened);
    }


    public static KeywordType of(String value) {
        return Stream.of(KeywordType.values())
            .filter(it->it.key.equals(value))
            .findAny()
            .orElseThrow();
    }

}
