package es.iti.wakamiti.plugins.gherkin.parser.elements;

public record Tag(Location location, String name) implements Node {
}
