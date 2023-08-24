package es.iti.wakamiti.plugins.gherkin.parser.elements;

import java.util.List;

public record Feature (
    Location location,
    List<Comment> comments,
    List<Tag> tags,
    String keyword,
    String name,
    String description,
    List<ScenarioDefinition> children,
    String language
) implements Section, ParentNode<ScenarioDefinition> { }
