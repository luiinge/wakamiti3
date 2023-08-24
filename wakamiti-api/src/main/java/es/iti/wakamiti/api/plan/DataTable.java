package es.iti.wakamiti.api.plan;

import java.util.List;
import java.util.function.UnaryOperator;

import lombok.*;

@Setter
@Getter
public final class DataTable implements NodeArgument {

    private List<List<String>> values;
    private int rows;
    private int columns;

    public DataTable() {
        this.values = List.of();
        this.rows  = 0;
        this.columns = 0;
    }


    public DataTable(List<List<String>> values) {
        if (values.stream().mapToInt(List::size).distinct().count() > 1) {
            throw new IllegalArgumentException(
                "All rows must have the same size. Values were:\n" + values
            );
        }
        this.values = values;
        this.rows = values.size();
        this.columns = values.get(0).size();
    }


    @Override
    public NodeArgument copy(UnaryOperator<String> replacingVariablesMethod) {
        return new DataTable(values.stream().map( row ->
            row.stream().map(replacingVariablesMethod).toList()
        ).toList());
    }



}
