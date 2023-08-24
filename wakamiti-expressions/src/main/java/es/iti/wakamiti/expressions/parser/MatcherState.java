package es.iti.wakamiti.expressions.parser;


import java.util.*;

import es.iti.wakamiti.expressions.Regex;


public class MatcherState {

    private final char[] text;
    private int position = 0;
    //private final Map<String, ExpressionArgument> arguments = new HashMap<>();
    private boolean rejected = false;
    private final Locale locale;


    public MatcherState(String text, Locale locale) {
        this.text = Regex.replace(text.strip(), "\\s+", " ").toCharArray();
        this.locale = locale;
    }


    public char[] chars() {
        return text;
    }


    public int position() {
        return this.position;
    }

    public void consume(int length) {
        this.position += length;
    }

    public void reject() {
        this.rejected = true;
    }

    public boolean totallyConsumed() {
        return position >= text.length;
    }


//    void addArgument(ExpressionArgument argument) {
//        if (arguments.containsKey(argument.name())) {
//            throw new KukumoException("Argument name {} is already used",argument.name());
//        }
//        arguments.put(argument.name(),argument);
//    }


    public boolean matches() {
        return !rejected && totallyConsumed();
    }

//
//    public ExpressionArgument argument(String name) {
//        return arguments.get(name);
//    }


    public Locale locale() {
        return locale;
    }


    public int remainingCharacters() {
        return text.length - position;
    }

}
