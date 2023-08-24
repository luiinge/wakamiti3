package es.iti.wakamiti.plugins.gherkin.parser.internal;

// http://rosettacode.org/wiki/String_length#Java
public class SymbolCounter {

    private SymbolCounter() { }

    public static int countSymbols(String string) {
        return string.codePointCount(0, string.length());
    }
}
