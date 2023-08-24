package es.iti.wakamiti.plugins.gherkin.parser.elements;

import java.util.List;

public record Examples(

    Location location,
    List<Comment> comments,
    List<Tag> tags,
    String keyword, String name,
    String description,
    TableRow tableHeader,
    List<TableRow> tableBody

) implements Section { }
