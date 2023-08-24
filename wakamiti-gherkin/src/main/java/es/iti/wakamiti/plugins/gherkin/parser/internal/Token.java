package es.iti.wakamiti.plugins.gherkin.parser.internal;

import java.util.List;

import es.iti.wakamiti.plugins.gherkin.parser.GherkinDialect;
import es.iti.wakamiti.plugins.gherkin.parser.elements.Location;
import lombok.*;


@Getter
@Setter
public class Token {

    private final GherkinLine line;
    private TokenType matchedType;
    private String matchedKeyword;
    private String matchedText;
    private List<GherkinLineSpan> matchedItems;
    private int matchedIndent;
    private GherkinDialect matchedGherkinDialect;
    private Location location;

    public Token(GherkinLine line, Location location) {
        this.line = line;
        this.location = location;
    }


    public int indent() {
        return line() == null ? 0 : line().indent();
    }


    public boolean isEOF() {
        return line == null;
    }


    public String getTokenValue() {
        return isEOF() ? "EOF" : line.getLineText(-1);
    }


    @Override
    public String toString() {
        return String.format("%s: %s/%s", matchedType, matchedKeyword, matchedText);
    }
}
