package es.iti.wakamiti.api;

import es.iti.wakamiti.api.contributors.DataTypeProvider;
import java.util.*;
import java.util.stream.*;

import jexten.ExtensionManager;


public class DataTypes {

    public static DataTypes of (DataType... dataTypes) {
        return new DataTypes(Arrays.asList(dataTypes));
    }


    private final Map<String,DataType> byName;
    private final List<String> allNames;


    public DataTypes(List<DataType> dataTypes) {
        this.byName = dataTypes.stream().collect(Collectors.toMap(DataType::name, e -> e));
        this.allNames = dataTypes.stream().map(DataType::name).sorted().toList();
    }


	DataTypes(ExtensionManager extensionManager) {
        var dataTypes = extensionManager.getExtensions(DataTypeProvider.class)
            .flatMap(DataTypeProvider::dataTypes)
            .toList();
        this.byName = dataTypes.stream().collect(Collectors.toMap(DataType::name, e -> e));
        this.allNames = dataTypes.stream().map(DataType::name).sorted().toList();
    }


    public DataType byName(String name) {
        DataType dataType = byName.get(name);
        if (dataType == null) {
            throw new WakamitiException("Unknown data type {}. Valid data types are: {}\n    ",
                name,
                String.join("\n    ",allNames())
            );
        }
        return dataType;
    }


    public List<String> allNames() {
        return allNames;
    }


    public Stream<DataType> stream() {
        return byName.values().stream();
    }

}
