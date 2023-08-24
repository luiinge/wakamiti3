package es.iti.wakamiti.api.plan;

import java.util.function.UnaryOperator;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public final class Document implements NodeArgument {

    private String contentType;
    private String content;

    @Override
    public NodeArgument copy(UnaryOperator<String> replacingVariablesMethod) {
        return new Document(contentType, replacingVariablesMethod.apply(content));
    }

}
