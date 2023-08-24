package es.iti.wakamiti.plugins.gherkin.parser.elements;

public interface Section extends Node, Tagged, Commented {
    String keyword();
    String name();
    String description();
}
