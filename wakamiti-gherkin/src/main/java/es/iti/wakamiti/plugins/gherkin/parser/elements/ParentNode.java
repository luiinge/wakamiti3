package es.iti.wakamiti.plugins.gherkin.parser.elements;

import java.util.List;

public interface ParentNode<T> {
    List<T> children();
}
