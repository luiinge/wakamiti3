package es.iti.wakamiti.plugins.gherkin.parser.elements;

import static java.util.Collections.unmodifiableList;

import java.util.List;

import lombok.*;

@EqualsAndHashCode
@Getter
@ToString
public abstract sealed class ScenarioDefinition implements Section, ParentNode<Step>
permits Background, Scenario, ScenarioOutline {

    private final Location location;
    private final List<Comment> comments;
    private final List<Tag> tags;
    private final String keyword;
    private final String name;
    private final String description;
    private final List<Step> children;


    protected ScenarioDefinition(
        Location location,
        List<Comment> comments,
        List<Tag> tags,
        String keyword,
        String name,
        String description,
        List<Step> children
    ) {
        this.location = location;
        this.comments = unmodifiableList(comments);
        this.tags = unmodifiableList(tags);
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.children = unmodifiableList(children);
    }


}
