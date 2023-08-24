package es.iti.wakamiti.api;

import java.util.*;
import java.util.stream.*;

public class SubExpressions {

    public static SubExpressions of (SubExpression... subExpressions) {
        return new SubExpressions(Arrays.asList(subExpressions));
    }

    private final Map<String, SubExpression> byName;
    private final List<String> allNames;


    public SubExpressions(List<SubExpression> subExpressions) {
        this.byName = subExpressions.stream().collect(Collectors.toMap(SubExpression::name, e -> e));
        this.allNames = subExpressions.stream().map(SubExpression::name).sorted().toList();
    }


//    SubExpressions(ExtensionManager extensionManager) {
//        var dataTypes = extensionManager.getExtensions(Subexpression.class).toList();
//        this.byName = dataTypes.stream().collect(Collectors.toMap(Subexpression::name, e -> e));
//        this.allNames = dataTypes.stream().map(Subexpression::name).sorted().toList();
//    }



    public SubExpression byName(String name) {
        SubExpression subExpression = byName.get(name);
        if (subExpression == null) {
            throw new WakamitiException("Unknown subexpression {}. Valid subexpression are: {}\n    ",
                name,
                String.join("\n    ",allNames())
            );
        }
        return subExpression;
    }


    public List<String> allNames() {
        return allNames;
    }


    public Stream<SubExpression> stream() {
        return byName.values().stream();
    }

}
