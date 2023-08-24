package es.iti.wakamiti.plugins.gherkin.parser.elements;

import java.util.List;

public record Step (
    Location location,
    List<Comment> comments,
    String keyword,
    String text,
    StepArgument argument
) implements Node, Commented { }