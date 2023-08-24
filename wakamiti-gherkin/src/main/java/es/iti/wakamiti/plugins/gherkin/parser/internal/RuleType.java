package es.iti.wakamiti.plugins.gherkin.parser.internal;

public enum RuleType {
    NONE,
    EOF, // #EOF
    EMPTY, // #Empty
    COMMENT, // #Comment
    TAG_LINE, // #TagLine
    FEATURE_LINE, // #FeatureLine
    BACKGROUND_LINE, // #BackgroundLine
    SCENARIO_LINE, // #ScenarioLine
    SCENARIO_OUTLINE_LINE, // #ScenarioOutlineLine
    EXAMPLES_LINE, // #ExamplesLine
    STEP_LINE, // #StepLine
    DOC_STRING_SEPARATOR, // #DocStringSeparator
    TABLE_ROW, // #TableRow
    LANGUAGE, // #Language
    OTHER, // #Other
    GHERKIN_DOCUMENT, // GherkinDocument! := Feature?
    FEATURE, // Feature! := Feature_Header Background? Scenario_Definition*
    FEATURE_HEADER, // Feature_Header! := #Language? Tags? #FeatureLine Description_Helper
    BACKGROUND, // Background! := #BackgroundLine Description_Helper Step*
    SCENARIO_DEFINITION, // Scenario_Definition! := Tags? (Scenario | ScenarioOutline)
    SCENARIO, // Scenario! := #ScenarioLine Description_Helper Step*
    SCENARIO_OUTLINE, // ScenarioOutline! := #ScenarioOutlineLine Description_Helper Step* Examples_Definition*
    EXAMPLES_DEFINITION, // Examples_Definition! [#Empty|#Comment|#TagLine-&gt;#ExamplesLine] := Tags? Examples
    EXAMPLES, // Examples! := #ExamplesLine Description_Helper Examples_Table?
    EXAMPLES_TABLE, // Examples_Table! := #TableRow #TableRow*
    STEP, // Step! := #StepLine Step_Arg?
    STEP_ARG, // Step_Arg := (DataTable | DocString)
    DATA_TABLE, // DataTable! := #TableRow+
    DOC_STRING, // DocString! := #DocStringSeparator #Other* #DocStringSeparator
    TAGS, // Tags! := #TagLine+
    DESCRIPTION_HELPER, // Description_Helper := #Empty* Description? #Comment*
    DESCRIPTION, // Description! := #Other+
    ;


    public static RuleType cast(TokenType tokenType) {
        return RuleType.values()[tokenType.ordinal()];
    }
}
