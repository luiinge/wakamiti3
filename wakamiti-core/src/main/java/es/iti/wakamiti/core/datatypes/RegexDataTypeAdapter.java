package es.iti.wakamiti.core.datatypes;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.DataType;
import es.iti.wakamiti.api.lang.ThrowableFunction;
import java.util.*;
import java.util.regex.Matcher;


public class RegexDataTypeAdapter<T> implements DataType {

    private final String name;
    private final String regex;
    private final Class<T> javaType;
    private final ThrowableFunction<String,T> parser;
    private final List<String> hints;


    public RegexDataTypeAdapter(
        String name,
        String regex,
        Class<T> javaType,
        ThrowableFunction<String, T> parser,
        List<String> hints
    ) {
        this.name = name;
        this.regex = regex;
        this.javaType = javaType;
        this.parser = parser;
        this.hints = hints;
    }


    @Override
    public String name() {
        return name;
    }


    @Override
    public Class<T> javaType() {
        return javaType;
    }


    @Override
    public String regex(Locale locale) {
        return regex;
    }


    @Override
    public List<String> hints(Locale locale) {
        return hints;
    }


    @Override
    public T parse(Locale locale, String value) {
        try {
            return parser.apply(value);
        } catch (Exception e) {
            throw new WakamitiException(e);
        }
    }


    @Override
    public Matcher matcher(Locale locale, String value) {
        return null;
    }

}
