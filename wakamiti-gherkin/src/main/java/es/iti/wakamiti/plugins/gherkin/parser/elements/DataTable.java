package es.iti.wakamiti.plugins.gherkin.parser.elements;

import java.util.*;

import lombok.*;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DataTable extends StepArgument {

    private final List<TableRow> rows;

    public DataTable(Location location, List<TableRow> rows) {
        super(location);
        this.rows = Collections.unmodifiableList(rows);
    }

    public DataTable(List<TableRow> rows) {
        this(rows.get(0).location(), rows);
    }


}
