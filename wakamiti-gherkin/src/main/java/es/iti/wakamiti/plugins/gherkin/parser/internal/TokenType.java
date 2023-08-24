package es.iti.wakamiti.plugins.gherkin.parser.internal;

public enum TokenType {
    NONE,
    EOF,
    EMPTY,
    COMMENT,
    TAG_LINE,
    FEATURE_LINE,
    BACKGROUND_LINE,
    SCENARIO_LINE,
    SCENARIO_OUTLINE_LINE,
    EXAMPLES_LINE,
    STEP_LINE,
    DOC_STRING_SEPARATOR,
    TABLE_ROW,
    LANGUAGE,
    OTHER,
    ;
}
