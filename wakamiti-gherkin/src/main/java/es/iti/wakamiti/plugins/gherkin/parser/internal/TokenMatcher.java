package es.iti.wakamiti.plugins.gherkin.parser.internal;


import static es.iti.wakamiti.plugins.gherkin.parser.KeywordType.*;
import java.util.*;
import java.util.regex.*;

import es.iti.wakamiti.plugins.gherkin.parser.*;
import es.iti.wakamiti.plugins.gherkin.parser.elements.Location;

public class TokenMatcher {

    private static final Pattern LANGUAGE_PATTERN = Pattern.compile(
        "^\\s*#\\s*language\\s*:\\s*([a-zA-Z\\-_]+)\\s*$"
    );

    private final GherkinDialectFactory dialectFactory;
    private GherkinDialect currentDialect;
    private String activeDocStringSeparator = null;
    private int indentToRemove = 0;


    private TokenMatcher(GherkinDialectFactory dialectProvider) {
        this.dialectFactory = dialectProvider;
        reset();
    }

    public TokenMatcher(KeywordMapProvider keywordMapProvider, String defaultDialect) {
        this(new GherkinDialectFactory(Objects.requireNonNull(keywordMapProvider),defaultDialect));
    }

    public TokenMatcher(KeywordMapProvider keywordMapProvider) {
        this(new GherkinDialectFactory(Objects.requireNonNull(keywordMapProvider),"en"));
    }


    public void reset() {
        activeDocStringSeparator = null;
        indentToRemove = 0;
        currentDialect = dialectFactory.defaultDialect();
    }

    
    public GherkinDialect getCurrentDialect() {
        return currentDialect;
    }

    protected void setTokenMatched(Token token, TokenType matchedType, String text, String keyword, Integer indent, List<GherkinLineSpan> items) {
        token.matchedType( matchedType );
        token.matchedKeyword( keyword );
        token.matchedText( text );
        token.matchedItems( items );
        token.matchedGherkinDialect( getCurrentDialect() );
        token.matchedIndent( indent != null ? indent : token.indent() );
        token.location( new Location(token.location().line(), token.matchedIndent() + 1) );
    }


    public boolean matchEOF(Token token) {
        if (token.isEOF()) {
            setTokenMatched(token, TokenType.EOF, null, null, null, null);
            return true;
        }
        return false;
    }


    public boolean matchOther(Token token) {
        String text = token.line().getLineText(indentToRemove); //take the entire line, except removing DocString indents
        setTokenMatched(token, TokenType.OTHER, unescapeDocString(text), null, 0, null);
        return true;
    }

    
    public boolean matchEmpty(Token token) {
        if (token.line().isEmpty()) {
            setTokenMatched(token, TokenType.EMPTY, null, null, null, null);
            return true;
        }
        return false;
    }

    
    public boolean matchComment(Token token) {
        if (token.line().startsWith(GherkinLanguageConstants.COMMENT_PREFIX)) {
            String text = token.line().getLineText(0); //take the entire line
            setTokenMatched(token, TokenType.COMMENT, text, null, 0, null);
            return true;
        }
        return false;
    }

    
    public boolean matchLanguage(Token token) {
        Matcher matcher = LANGUAGE_PATTERN.matcher(token.line().getLineText(0));
        if (matcher.matches()) {
            String language = matcher.group(1);
            setTokenMatched(token, TokenType.LANGUAGE, language, null, null, null);
            currentDialect = dialectFactory.dialectFor(language);
            return true;
        }
        return false;
    }

    
    public boolean matchTagLine(Token token) {
        if (token.line().startsWith(GherkinLanguageConstants.TAG_PREFIX)) {
            setTokenMatched(token, TokenType.TAG_LINE, null, null, null, token.line().getTags());
            return true;
        }
        return false;
    }

    
    public boolean matchFeatureLine(Token token) {
        return matchTitleLine(token, TokenType.FEATURE_LINE, currentDialect.keywords(FEATURE));
    }

    
    public boolean matchBackgroundLine(Token token) {
        return matchTitleLine(token, TokenType.BACKGROUND_LINE, currentDialect.keywords(BACKGROUND));
    }

    
    public boolean matchScenarioLine(Token token) {
        return matchTitleLine(token, TokenType.SCENARIO_LINE, currentDialect.keywords(SCENARIO));
    }

    
    public boolean matchScenarioOutlineLine(Token token) {
        return matchTitleLine(token, TokenType.SCENARIO_OUTLINE_LINE, currentDialect.keywords(SCENARIO_OUTLINE));
    }

    
    public boolean matchExamplesLine(Token token) {
        return matchTitleLine(token, TokenType.EXAMPLES_LINE, currentDialect.keywords(EXAMPLES));
    }

    private boolean matchTitleLine(Token token, TokenType tokenType, List<String> keywords) {
        for (String keyword : keywords) {
            if (token.line().startsWithTitleKeyword(keyword)) {
                String title = token.line().getRestTrimmed(keyword.length() + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length());
                setTokenMatched(token, tokenType, title, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    
    public boolean matchDocStringSeparator(Token token) {
        return activeDocStringSeparator == null
                // open
                ? matchDocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_SEPARATOR, true) ||
                matchDocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_ALTERNATIVE_SEPARATOR, true)
                // close
                : matchDocStringSeparator(token, activeDocStringSeparator, false);
    }

    private boolean matchDocStringSeparator(Token token, String separator, boolean isOpen) {
        if (token.line().startsWith(separator)) {
            String contentType = null;
            if (isOpen) {
                contentType = token.line().getRestTrimmed(separator.length());
                activeDocStringSeparator = separator;
                indentToRemove = token.line().indent();
            } else {
                activeDocStringSeparator = null;
                indentToRemove = 0;
            }

            setTokenMatched(token, TokenType.DOC_STRING_SEPARATOR, contentType, null, null, null);
            return true;
        }
        return false;
    }

    
    public boolean matchStepLine(Token token) {
        List<String> keywords = currentDialect.keywords(STEP);
        for (String keyword : keywords) {
            if (token.line().startsWith(keyword)) {
                String stepText = token.line().getRestTrimmed(keyword.length());
                setTokenMatched(token, TokenType.STEP_LINE, stepText, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    
    public boolean matchTableRow(Token token) {
        if (token.line().startsWith(GherkinLanguageConstants.TABLE_CELL_SEPARATOR)) {
            setTokenMatched(token, TokenType.TABLE_ROW, null, null, null, token.line().getTableCells());
            return true;
        }
        return false;
    }

    private String unescapeDocString(String text) {
        return activeDocStringSeparator != null ? text.replace("\\\"\\\"\\\"", "\"\"\"") : text;
    }
}
