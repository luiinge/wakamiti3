package es.iti.wakamiti.plugins.gherkin.parser.internal;

import java.util.Locale;

import es.iti.wakamiti.plugins.gherkin.parser.ParserException;
import es.iti.wakamiti.plugins.gherkin.parser.elements.Location;

public class NoSuchLanguageException extends ParserException {

    public NoSuchLanguageException(Locale locale, Location location) {
        super("Language not supported: " + locale, location);
    }

}
