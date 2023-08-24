package es.iti.wakamiti.plugins.gherkin.parser.internal;

import java.util.List;

import es.iti.wakamiti.plugins.gherkin.parser.ParserException;


public class UnexpectedEOFException extends ParserException {

    public final String stateComment;
    public final transient List<String> expectedTokenTypes;


    public UnexpectedEOFException(Token receivedToken, List<String> expectedTokenTypes, String stateComment) {
        super(getMessage(expectedTokenTypes), receivedToken.location());
        this.expectedTokenTypes = expectedTokenTypes;
        this.stateComment = stateComment;
    }


    private static String getMessage(List<String> expectedTokenTypes) {
        return String.format("unexpected end of file, expected: %s",
            String.join(", ", expectedTokenTypes));
    }

}
