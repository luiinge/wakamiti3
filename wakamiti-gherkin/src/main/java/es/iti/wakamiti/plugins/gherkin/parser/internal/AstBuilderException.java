package es.iti.wakamiti.plugins.gherkin.parser.internal;


import es.iti.wakamiti.plugins.gherkin.parser.ParserException;
import es.iti.wakamiti.plugins.gherkin.parser.elements.Location;

public class AstBuilderException extends ParserException {

    public AstBuilderException(String message, Location location) {
        super(message, location);
    }

}
