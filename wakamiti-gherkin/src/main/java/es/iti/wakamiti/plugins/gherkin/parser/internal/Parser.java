package es.iti.wakamiti.plugins.gherkin.parser.internal;


import java.io.*;
import static java.util.Arrays.asList;

import java.util.*;
import java.util.function.*;

import es.iti.wakamiti.plugins.gherkin.parser.*;
import es.iti.wakamiti.plugins.gherkin.parser.elements.GherkinDocument;


public class Parser {

    private static final String EOF = "#EOF";
    private static final String EMPTY = "#Empty";
    private static final String COMMENT = "#Comment";
    private static final String STEP_LINE = "#StepLine";
    private static final String TAG_LINE = "#TagLine";
    private static final String EXAMPLES_LINE = "#ExamplesLine";
    private static final String SCENARIO_LINE = "#ScenarioLine";
    private static final String SCENARIO_OUTLINE_LINE = "#ScenarioOutlineLine";
    private static final String OTHER = "#Other";
    private static final String TABLE_ROW = "#TableRow";
    private static final String DOC_STRING_SEPARATOR = "#DocStringSeparator";
    private static final String BACKGROUND_LINE = "#BackgroundLine";
    private static final String FEATURE_LINE = "#FeatureLine";
    private static final String LANGUAGE = "#Language";

    private final List<ToIntBiFunction<Token, ParserContext>> stateMatchers = List.of(
        this::matchTokenAt0,
        this::matchTokenAt1,
        this::matchTokenAt2,
        this::matchTokenAt3,
        this::matchTokenAt4,
        this::matchTokenAt5,
        this::matchTokenAt6,
        this::matchTokenAt7,
        this::matchTokenAt8,
        this::matchTokenAt9,
        this::matchTokenAt10,
        this::matchTokenAt11,
        this::matchTokenAt12,
        this::matchTokenAt13,
        this::matchTokenAt14,
        this::matchTokenAt15,
        this::matchTokenAt16,
        this::matchTokenAt17,
        this::matchTokenAt18,
        this::matchTokenAt19,
        this::matchTokenAt20,
        this::matchTokenAt21,
        this::matchTokenAt22,
        this::matchTokenAt23,
        this::matchTokenAt24,
        this::matchTokenAt25,
        this::matchTokenAt26,
        (a,b)->0,
        this::matchTokenAt28,
        this::matchTokenAt29,
        this::matchTokenAt30,
        this::matchTokenAt31,
        this::matchTokenAt32,
        this::matchTokenAt33
    );

    record ParserContext(
        TokenScanner tokenScanner,
        TokenMatcher tokenMatcher,
        Queue<Token> tokenQueue,
        List<ParserException> errors
    ) { }

    private static final boolean STOP_AT_FIRST_ERROR = false;

    private final KeywordMapProvider keywordMapProvider;
    private final GherkinAstBuilder builder = new GherkinAstBuilder();



    public Parser(KeywordMapProvider keywordMapProvider) {
        this.keywordMapProvider = Objects.requireNonNull(
            keywordMapProvider,
            "keywordMapProvider cannot be null"
        );
    }


    public GherkinDocument parse(Reader source) {
        return parse(new TokenScanner(source));
    }

    public GherkinDocument parse(InputStream inputStream) {
        return parse(new TokenScanner(inputStream));
    }


    public GherkinDocument parse(TokenScanner tokenScanner) {
        return parse(tokenScanner, new TokenMatcher(keywordMapProvider));
    }


    public GherkinDocument parse(TokenScanner tokenScanner, TokenMatcher tokenMatcher) {

        builder.reset();
        tokenMatcher.reset();

        ParserContext context = new ParserContext(
            tokenScanner, tokenMatcher, new LinkedList<>(), new ArrayList<>()
        );

        startRule(context, RuleType.GHERKIN_DOCUMENT);
        int state = 0;
        Token token;
        do {
            token = readToken(context);
            state = matchToken(state, token, context);
        } while (!token.isEOF());

        endRule(context);

        if (!context.errors.isEmpty()) {
            throw new ParserException.CompositeParserException(context.errors);
        }

        return builder.getResult();
    }


    private void addError(ParserContext context, ParserException error) {
        context.errors.add(error);
        if (context.errors.size() > 10)
            throw new ParserException.CompositeParserException(context.errors);
    }


    private void handleAstError(ParserContext context, final Runnable action) {
        handleExternalError(context, () -> {
            action.run();
            return null;
        }, null);
    }


    private <V> V handleExternalError(ParserContext context, Supplier<V> action, V defaultValue) {
        if (STOP_AT_FIRST_ERROR) {
            return action.get();
        }

        try {
            return action.get();
        } catch (ParserException.CompositeParserException compositeParserException) {
            for (ParserException error : compositeParserException.getErrors()) {
                addError(context, error);
            }
        } catch (ParserException error) {
            addError(context, error);
        }
        return defaultValue;
    }


    private void build(final ParserContext context, final Token token) {
        handleAstError(context, () -> builder.build(token));
    }


    private void startRule(final ParserContext context, final RuleType ruleType) {
        handleAstError(context, () -> builder.startRule(ruleType));
    }


    private void endRule(final ParserContext context) {
        handleAstError(context, builder::endRule);
    }


    private Token readToken(ParserContext context) {
        return !context.tokenQueue.isEmpty() ? context.tokenQueue.remove()
            : context.tokenScanner.read();
    }


    private boolean matchEOF(final ParserContext context, final Token token) {
        return handleExternalError(context, () -> context.tokenMatcher.matchEOF(token), false);
    }


    private boolean matchEmpty(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(context, () -> context.tokenMatcher.matchEmpty(token), false);
    }


    private boolean matchComment(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(context, () -> context.tokenMatcher.matchComment(token), false);
    }


    private boolean matchTagLine(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(context, () -> context.tokenMatcher.matchTagLine(token), false);
    }


    private boolean matchFeatureLine(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(
            context,
            () -> context.tokenMatcher.matchFeatureLine(token),
            false
        );
    }


    private boolean matchBackgroundLine(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(
            context,
            () -> context.tokenMatcher.matchBackgroundLine(token),
            false
        );
    }


    private boolean matchScenarioLine(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(
            context,
            () -> context.tokenMatcher.matchScenarioLine(token),
            false
        );
    }


    private boolean matchScenarioOutlineLine(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(
            context,
            () -> context.tokenMatcher.matchScenarioOutlineLine(token),
            false
        );
    }


    private boolean matchExamplesLine(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(
            context,
            () -> context.tokenMatcher.matchExamplesLine(token),
            false
        );
    }


    private boolean matchStepLine(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(context, () -> context.tokenMatcher.matchStepLine(token), false);
    }


    private boolean matchDocStringSeparator(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(
            context,
            () -> context.tokenMatcher.matchDocStringSeparator(token),
            false
        );
    }


    private boolean matchTableRow(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(context, () -> context.tokenMatcher.matchTableRow(token), false);
    }


    private boolean matchLanguage(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(context, () -> context.tokenMatcher.matchLanguage(token), false);
    }


    private boolean matchOther(final ParserContext context, final Token token) {
        if (token.isEOF())
            return false;
        return handleExternalError(context, () -> context.tokenMatcher.matchOther(token), false);
    }


    private int matchToken(int state, Token token, ParserContext context) {
        if (state > stateMatchers.size() - 1) {
            throw new IllegalStateException("Unknown state: " + state);
        }
        return stateMatchers.get(state).applyAsInt(token,context);
    }


    // Start
    private int matchTokenAt0(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            build(context, token);
            return 27;
        }
        if (matchLanguage(context, token)) {
            startRule(context, RuleType.FEATURE);
            startRule(context, RuleType.FEATURE_HEADER);
            build(context, token);
            return 1;
        }
        if (matchTagLine(context, token)) {
            startRule(context, RuleType.FEATURE);
            startRule(context, RuleType.FEATURE_HEADER);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 2;
        }
        if (matchFeatureLine(context, token)) {
            startRule(context, RuleType.FEATURE);
            startRule(context, RuleType.FEATURE_HEADER);
            build(context, token);
            return 3;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 0;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 0;
        }

        final String stateComment = "State: 0 - Start";

        List<String> expectedTokens = asList(
            EOF,
            LANGUAGE,
            TAG_LINE,
            FEATURE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 0;

    }


    // GherkinDocument:0>Feature:0>Feature_Header:0>#Language:0
    private int matchTokenAt1(Token token, ParserContext context) {
        if (matchTagLine(context, token)) {
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 2;
        }
        if (matchFeatureLine(context, token)) {
            build(context, token);
            return 3;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 1;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 1;
        }

        final String stateComment = "State: 1 - GherkinDocument:0>Feature:0>Feature_Header:0>#Language:0";

        List<String> expectedTokens = asList(TAG_LINE, FEATURE_LINE, COMMENT, EMPTY);
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 1;

    }


    // GherkinDocument:0>Feature:0>Feature_Header:1>Tags:0>#TagLine:0
    private int matchTokenAt2(Token token, ParserContext context) {
        if (matchTagLine(context, token)) {
            build(context, token);
            return 2;
        }
        if (matchFeatureLine(context, token)) {
            endRule(context);
            build(context, token);
            return 3;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 2;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 2;
        }

        final String stateComment = "State: 2 - GherkinDocument:0>Feature:0>Feature_Header:1>Tags:0>#TagLine:0";

        List<String> expectedTokens = asList(TAG_LINE, FEATURE_LINE, COMMENT, EMPTY);
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 2;

    }


    // GherkinDocument:0>Feature:0>Feature_Header:2>#FeatureLine:0
    private int matchTokenAt3(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 3;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 5;
        }
        if (matchBackgroundLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.BACKGROUND);
            build(context, token);
            return 6;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            startRule(context, RuleType.DESCRIPTION);
            build(context, token);
            return 4;
        }

        final String stateComment = "State: 3 - GherkinDocument:0>Feature:0>Feature_Header:2>#FeatureLine:0";

        List<String> expectedTokens = asList(
            EOF,
            EMPTY,
            COMMENT,
            BACKGROUND_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 3;

    }


    // GherkinDocument:0>Feature:0>Feature_Header:3>DescriptionHelper:1>Description:0>#Other:0
    private int matchTokenAt4(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            endRule(context);
            build(context, token);
            return 5;
        }
        if (matchBackgroundLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.BACKGROUND);
            build(context, token);
            return 6;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 4;
        }

        final String stateComment = "State: 4 - GherkinDocument:0>Feature:0>Feature_Header:3>DescriptionHelper:1>Description:0>#Other:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            BACKGROUND_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 4;

    }


    // GherkinDocument:0>Feature:0>Feature_Header:3>DescriptionHelper:2>#Comment:0
    private int matchTokenAt5(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 5;
        }
        if (matchBackgroundLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.BACKGROUND);
            build(context, token);
            return 6;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 5;
        }

        final String stateComment = "State: 5 - GherkinDocument:0>Feature:0>Feature_Header:3>DescriptionHelper:2>#Comment:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            BACKGROUND_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 5;

    }


    // GherkinDocument:0>Feature:1>Background:0>#BackgroundLine:0
    private int matchTokenAt6(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 6;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 8;
        }
        if (matchStepLine(context, token)) {
            startRule(context, RuleType.STEP);
            build(context, token);
            return 9;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            startRule(context, RuleType.DESCRIPTION);
            build(context, token);
            return 7;
        }

        final String stateComment = "State: 6 - GherkinDocument:0>Feature:1>Background:0>#BackgroundLine:0";

        List<String> expectedTokens = asList(
            EOF,
            EMPTY,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 6;

    }


    // GherkinDocument:0>Feature:1>Background:1>DescriptionHelper:1>Description:0>#Other:0
    private int matchTokenAt7(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            endRule(context);
            build(context, token);
            return 8;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 9;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 7;
        }

        final String stateComment = "State: 7 - GherkinDocument:0>Feature:1>Background:1>DescriptionHelper:1>Description:0>#Other:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 7;

    }


    // GherkinDocument:0>Feature:1>Background:1>DescriptionHelper:2>#Comment:0
    private int matchTokenAt8(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 8;
        }
        if (matchStepLine(context, token)) {
            startRule(context, RuleType.STEP);
            build(context, token);
            return 9;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 8;
        }

        final String stateComment = "State: 8 - GherkinDocument:0>Feature:1>Background:1>DescriptionHelper:2>#Comment:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 8;

    }


    // GherkinDocument:0>Feature:1>Background:2>Step:0>#StepLine:0
    private int matchTokenAt9(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchTableRow(context, token)) {
            startRule(context, RuleType.DATA_TABLE);
            build(context, token);
            return 10;
        }
        if (matchDocStringSeparator(context, token)) {
            startRule(context, RuleType.DOC_STRING);
            build(context, token);
            return 32;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 9;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 9;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 9;
        }

        final String stateComment = "State: 9 - GherkinDocument:0>Feature:1>Background:2>Step:0>#StepLine:0";

        List<String> expectedTokens = asList(
            EOF,
            TABLE_ROW,
            DOC_STRING_SEPARATOR,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 9;

    }


    // GherkinDocument:0>Feature:1>Background:2>Step:1>StepArg:0>alt1:0>DataTable:0>#TableRow:0
    private int matchTokenAt10(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchTableRow(context, token)) {
            build(context, token);
            return 10;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 9;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 10;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 10;
        }

        final String stateComment = "State: 10 - GherkinDocument:0>Feature:1>Background:2>Step:1>StepArg:0>alt1:0>DataTable:0>#TableRow:0";

        List<String> expectedTokens = asList(
            EOF,
            TABLE_ROW,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 10;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:0>Tags:0>#TagLine:0
    private int matchTokenAt11(Token token, ParserContext context) {
        if (matchTagLine(context, token)) {
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 11;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 11;
        }

        final String stateComment = "State: 11 - GherkinDocument:0>Feature:2>Scenario_Definition:0>Tags:0>#TagLine:0";

        List<String> expectedTokens = asList(
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 11;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:0>#ScenarioLine:0
    private int matchTokenAt12(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 12;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 14;
        }
        if (matchStepLine(context, token)) {
            startRule(context, RuleType.STEP);
            build(context, token);
            return 15;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            startRule(context, RuleType.DESCRIPTION);
            build(context, token);
            return 13;
        }

        final String stateComment = "State: 12 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:0>#ScenarioLine:0";

        List<String> expectedTokens = asList(
            EOF,
            EMPTY,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 12;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:1>DescriptionHelper:1>Description:0>#Other:0
    private int matchTokenAt13(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            endRule(context);
            build(context, token);
            return 14;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 15;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 13;
        }

        final String stateComment = "State: 13 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:1>DescriptionHelper:1>Description:0>#Other:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 13;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:1>DescriptionHelper:2>#Comment:0
    private int matchTokenAt14(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 14;
        }
        if (matchStepLine(context, token)) {
            startRule(context, RuleType.STEP);
            build(context, token);
            return 15;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 14;
        }

        final String stateComment = "State: 14 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:1>DescriptionHelper:2>#Comment:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 14;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:0>#StepLine:0
    private int matchTokenAt15(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchTableRow(context, token)) {
            startRule(context, RuleType.DATA_TABLE);
            build(context, token);
            return 16;
        }
        if (matchDocStringSeparator(context, token)) {
            startRule(context, RuleType.DOC_STRING);
            build(context, token);
            return 30;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 15;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 15;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 15;
        }

        final String stateComment = "State: 15 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:0>#StepLine:0";

        List<String> expectedTokens = asList(
            EOF,
            TABLE_ROW,
            DOC_STRING_SEPARATOR,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 15;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:1>StepArg:0>alt1:0>DataTable:0>#TableRow:0
    private int matchTokenAt16(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchTableRow(context, token)) {
            build(context, token);
            return 16;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 15;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 16;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 16;
        }

        final String stateComment = "State: 16 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:1>StepArg:0>alt1:0>DataTable:0>#TableRow:0";

        List<String> expectedTokens = asList(
            EOF,
            TABLE_ROW,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 16;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:0>#ScenarioOutlineLine:0
    private int matchTokenAt17(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 19;
        }
        if (matchStepLine(context, token)) {
            startRule(context, RuleType.STEP);
            build(context, token);
            return 20;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            startRule(context, RuleType.DESCRIPTION);
            build(context, token);
            return 18;
        }

        final String stateComment = "State: 17 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:0>#ScenarioOutlineLine:0";

        List<String> expectedTokens = asList(
            EOF,
            EMPTY,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 17;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:1>DescriptionHelper:1>Description:0>#Other:0
    private int matchTokenAt18(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            endRule(context);
            build(context, token);
            return 19;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 20;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 18;
        }

        final String stateComment = "State: 18 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:1>DescriptionHelper:1>Description:0>#Other:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 18;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:1>DescriptionHelper:2>#Comment:0
    private int matchTokenAt19(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 19;
        }
        if (matchStepLine(context, token)) {
            startRule(context, RuleType.STEP);
            build(context, token);
            return 20;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 19;
        }

        final String stateComment = "State: 19 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:1>DescriptionHelper:2>#Comment:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            STEP_LINE,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 19;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:0>#StepLine:0
    private int matchTokenAt20(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchTableRow(context, token)) {
            startRule(context, RuleType.DATA_TABLE);
            build(context, token);
            return 21;
        }
        if (matchDocStringSeparator(context, token)) {
            startRule(context, RuleType.DOC_STRING);
            build(context, token);
            return 28;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 20;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 20;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 20;
        }

        final String stateComment = "State: 20 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:0>#StepLine:0";

        List<String> expectedTokens = asList(
            EOF,
            TABLE_ROW,
            DOC_STRING_SEPARATOR,
            STEP_LINE,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 20;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:1>StepArg:0>alt1:0>DataTable:0>#TableRow:0
    private int matchTokenAt21(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchTableRow(context, token)) {
            build(context, token);
            return 21;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 20;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 21;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 21;
        }

        final String stateComment = "State: 21 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:1>StepArg:0>alt1:0>DataTable:0>#TableRow:0";

        List<String> expectedTokens = asList(
            EOF,
            TABLE_ROW,
            STEP_LINE,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 21;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:0>Tags:0>#TagLine:0
    private int matchTokenAt22(Token token, ParserContext context) {
        if (matchTagLine(context, token)) {
            build(context, token);
            return 22;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 22;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 22;
        }

        final String stateComment = "State: 22 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:0>Tags:0>#TagLine:0";

        List<String> expectedTokens = asList(TAG_LINE, EXAMPLES_LINE, COMMENT, EMPTY);
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 22;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:0>#ExamplesLine:0
    private int matchTokenAt23(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 23;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 25;
        }
        if (matchTableRow(context, token)) {
            startRule(context, RuleType.EXAMPLES_TABLE);
            build(context, token);
            return 26;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            startRule(context, RuleType.DESCRIPTION);
            build(context, token);
            return 24;
        }

        final String stateComment = "State: 23 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:0>#ExamplesLine:0";

        List<String> expectedTokens = asList(
            EOF,
            EMPTY,
            COMMENT,
            TABLE_ROW,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 23;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:1>DescriptionHelper:1>Description:0>#Other:0
    private int matchTokenAt24(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            endRule(context);
            build(context, token);
            return 25;
        }
        if (matchTableRow(context, token)) {
            endRule(context);
            startRule(context, RuleType.EXAMPLES_TABLE);
            build(context, token);
            return 26;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 24;
        }

        final String stateComment = "State: 24 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:1>DescriptionHelper:1>Description:0>#Other:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            TABLE_ROW,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            OTHER
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 24;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:1>DescriptionHelper:2>#Comment:0
    private int matchTokenAt25(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 25;
        }
        if (matchTableRow(context, token)) {
            startRule(context, RuleType.EXAMPLES_TABLE);
            build(context, token);
            return 26;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 25;
        }

        final String stateComment = "State: 25 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:1>DescriptionHelper:2>#Comment:0";

        List<String> expectedTokens = asList(
            EOF,
            COMMENT,
            TABLE_ROW,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 25;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:2>Examples_Table:0>#TableRow:0
    private int matchTokenAt26(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchTableRow(context, token)) {
            build(context, token);
            return 26;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 26;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 26;
        }

        final String stateComment = "State: 26 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:3>Examples_Definition:1>Examples:2>Examples_Table:0>#TableRow:0";

        List<String> expectedTokens = asList(
            EOF,
            TABLE_ROW,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 26;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:1>StepArg:0>alt1:1>DocString:0>#DocStringSeparator:0
    private int matchTokenAt28(Token token, ParserContext context) {
        if (matchDocStringSeparator(context, token)) {
            build(context, token);
            return 29;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 28;
        }

        final String stateComment = "State: 28 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:1>StepArg:0>alt1:1>DocString:0>#DocStringSeparator:0";

        List<String> expectedTokens = asList(DOC_STRING_SEPARATOR, OTHER);
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 28;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:1>StepArg:0>alt1:1>DocString:2>#DocStringSeparator:0
    private int matchTokenAt29(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 20;
        }
        if (matchTagLine(context, token) && lookahead0(context)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 22;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchExamplesLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.EXAMPLES_DEFINITION);
            startRule(context, RuleType.EXAMPLES);
            build(context, token);
            return 23;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 29;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 29;
        }

        final String stateComment = "State: 29 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:1>ScenarioOutline:2>Step:1>StepArg:0>alt1:1>DocString:2>#DocStringSeparator:0";

        List<String> expectedTokens = asList(
            EOF,
            STEP_LINE,
            TAG_LINE,
            EXAMPLES_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 29;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:1>StepArg:0>alt1:1>DocString:0>#DocStringSeparator:0
    private int matchTokenAt30(Token token, ParserContext context) {
        if (matchDocStringSeparator(context, token)) {
            build(context, token);
            return 31;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 30;
        }

        final String stateComment = "State: 30 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:1>StepArg:0>alt1:1>DocString:0>#DocStringSeparator:0";

        List<String> expectedTokens = asList(DOC_STRING_SEPARATOR, OTHER);
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 30;

    }


    // GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:1>StepArg:0>alt1:1>DocString:2>#DocStringSeparator:0
    private int matchTokenAt31(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 15;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 31;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 31;
        }

        final String stateComment = "State: 31 - GherkinDocument:0>Feature:2>Scenario_Definition:1>alt0:0>Scenario:2>Step:1>StepArg:0>alt1:1>DocString:2>#DocStringSeparator:0";

        List<String> expectedTokens = asList(
            EOF,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 31;

    }


    // GherkinDocument:0>Feature:1>Background:2>Step:1>StepArg:0>alt1:1>DocString:0>#DocStringSeparator:0
    private int matchTokenAt32(Token token, ParserContext context) {
        if (matchDocStringSeparator(context, token)) {
            build(context, token);
            return 33;
        }
        if (matchOther(context, token)) {
            build(context, token);
            return 32;
        }

        final String stateComment = "State: 32 - GherkinDocument:0>Feature:1>Background:2>Step:1>StepArg:0>alt1:1>DocString:0>#DocStringSeparator:0";

        List<String> expectedTokens = asList(DOC_STRING_SEPARATOR, OTHER);
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 32;

    }


    // GherkinDocument:0>Feature:1>Background:2>Step:1>StepArg:0>alt1:1>DocString:2>#DocStringSeparator:0
    private int matchTokenAt33(Token token, ParserContext context) {
        if (matchEOF(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            endRule(context);
            build(context, token);
            return 27;
        }
        if (matchStepLine(context, token)) {
            endRule(context);
            endRule(context);
            startRule(context, RuleType.STEP);
            build(context, token);
            return 9;
        }
        if (matchTagLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.TAGS);
            build(context, token);
            return 11;
        }
        if (matchScenarioLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO);
            build(context, token);
            return 12;
        }
        if (matchScenarioOutlineLine(context, token)) {
            endRule(context);
            endRule(context);
            endRule(context);
            startRule(context, RuleType.SCENARIO_DEFINITION);
            startRule(context, RuleType.SCENARIO_OUTLINE);
            build(context, token);
            return 17;
        }
        if (matchComment(context, token)) {
            build(context, token);
            return 33;
        }
        if (matchEmpty(context, token)) {
            build(context, token);
            return 33;
        }

        final String stateComment = "State: 33 - GherkinDocument:0>Feature:1>Background:2>Step:1>StepArg:0>alt1:1>DocString:2>#DocStringSeparator:0";

        List<String> expectedTokens = asList(
            EOF,
            STEP_LINE,
            TAG_LINE,
            SCENARIO_LINE,
            SCENARIO_OUTLINE_LINE,
            COMMENT,
            EMPTY
        );
        ParserException error = token.isEOF()
            ? new UnexpectedEOFException(token, expectedTokens, stateComment)
            : new UnexpectedTokenException(token, expectedTokens);
        if (STOP_AT_FIRST_ERROR)
            throw error;

        addError(context, error);
        return 33;

    }


    private boolean lookahead0(ParserContext context) {
        Token token;
        Queue<Token> queue = new ArrayDeque<>();
        boolean match = false;
        do {
            token = readToken(context);
            queue.add(token);
            if (matchExamplesLine(context, token)) {
                match = true;
                break;
            }
        } while (matchEmpty(context, token) || matchComment(context, token) || matchTagLine(context, token));
        context.tokenQueue.addAll(queue);
        return match;
    }


}
