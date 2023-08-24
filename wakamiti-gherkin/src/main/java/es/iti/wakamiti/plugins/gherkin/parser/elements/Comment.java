package es.iti.wakamiti.plugins.gherkin.parser.elements;

public record Comment(Location location, String text) implements  Node {

}
