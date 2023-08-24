package es.iti.wakamiti.plugins.gherkin.parser;

import java.util.*;
import java.util.stream.Collectors;

import es.iti.wakamiti.plugins.gherkin.parser.elements.Location;

public class ParserException extends RuntimeException {


    private final transient Location location;

    public ParserException(Throwable throwable, String message) {
        super(message, throwable);
        location = null;
    }

    protected ParserException(String message) {
        super(message);
        location = null;
    }

    protected ParserException(String message, Location location) {
        super(getMessage(message, location));
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    private static String getMessage(String message, Location location) {
        return String.format("(%s:%s): %s", location.line(), location.column(), message);
    }


    public static class CompositeParserException extends ParserException {

        private final List<ParserException> errors;

        public CompositeParserException(List<ParserException> errors) {
            super(getMessage(errors));
            this.errors = Collections.unmodifiableList(errors);
        }

        private static String getMessage(List<ParserException> errors) {
            if (errors == null) throw new NullPointerException("errors");
            return errors.stream().map(Throwable::getMessage).collect(Collectors.joining("\n","Parser errors:\n",""));
        }

        public List<ParserException> getErrors() {
            return List.copyOf(errors);
        }
    }
}
