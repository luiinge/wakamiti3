package es.iti.wakamiti.expressions.parser;

import java.util.*;

import es.iti.wakamiti.expressions.ExpressionException;

public class Tokenizer {

    private static final char NEGATION_SYMBOL = '^';
    private static final char START_OPTIONAL_SYMBOL = '(';
    private static final char CHOICE_SYMBOL = '|';
    private static final char START_GROUP_SYMBOL = '[';
    private static final char WILDCARD_SYMBOL = '*';
    private static final char START_ARGUMENT_SYMBOL = '{';
    private static final char END_GROUP_SYMBOL = ']';
    private static final char END_OPTIONAL_SYMBOL = ')';
    private static final char END_ARGUMENT_SYMBOL = '}';
    private static final char ESCAPE_SYMBOL = '\\';

    static final char[] symbols = new char[] {
        WILDCARD_SYMBOL,
        ESCAPE_SYMBOL,
        START_OPTIONAL_SYMBOL,
        END_OPTIONAL_SYMBOL,
        START_ARGUMENT_SYMBOL,
        END_ARGUMENT_SYMBOL,
        NEGATION_SYMBOL,
        START_GROUP_SYMBOL,
        END_GROUP_SYMBOL,
        CHOICE_SYMBOL
    };

    static {
        Arrays.sort(symbols);
    }

    private static boolean isSymbol(char c) {
        return Arrays.binarySearch(symbols,c) >= 0;
    }


    static final Map<Character,TokenType> tokenTypeBySymbol = Map.ofEntries(
        Map.entry(NEGATION_SYMBOL, TokenType.NEGATION),
        Map.entry(START_OPTIONAL_SYMBOL, TokenType.START_OPTIONAL),
        Map.entry(END_OPTIONAL_SYMBOL, TokenType.END_OPTIONAL),
        Map.entry(WILDCARD_SYMBOL, TokenType.WILDCARD),
        Map.entry(START_ARGUMENT_SYMBOL, TokenType.START_ARGUMENT),
        Map.entry(END_ARGUMENT_SYMBOL, TokenType.END_ARGUMENT),
        Map.entry(START_GROUP_SYMBOL, TokenType.START_GROUP),
        Map.entry(END_GROUP_SYMBOL, TokenType.END_GROUP),
        Map.entry(CHOICE_SYMBOL, TokenType.CHOICE_SEPARATOR)
    );


    private final String text;
    private final StringBuilder buffer;
    private boolean escaped = false;
    private int position = 0;
    private int tokenStart = 0;
    private List<Token> tokens = null;


    Tokenizer(String text) {
        this.text = text.trim();
        this.buffer = new StringBuilder(text.length());
    }


    List<Token> tokens() {
        if (tokens == null) {
            tokens = new ArrayList<>();
            while (hasNext()) {
                next();
            }
            dumpBuffer();
        }
        return tokens;
     }


    private void next() {

        char current = text.charAt(position);
        char next = (position == text.length() - 1 ? 0 : text.charAt(position + 1));

        if (escaped) {
            if (isSymbol(current)) {
                buffer.append(current);
                escaped = false;
                position++;
                return;
            } else {
                abort("unexpected escaped character "+ current);
            }
        }

        if (current == ESCAPE_SYMBOL) {
            escaped = true;
            position++;
            return;
        }

        if (Character.isWhitespace(current) && Character.isWhitespace(next)) {
            position++;
            return;
        }

        if (isSymbol(current)) {
            dumpBuffer();
            if (current == START_ARGUMENT_SYMBOL) {
                if (next == START_ARGUMENT_SYMBOL) {
                    addToken(TokenType.START_SUBEXPRESSION);
                    position++;
                } else {
                    addToken(TokenType.START_ARGUMENT);
                }
            } else if (current == END_ARGUMENT_SYMBOL) {
                if (next == END_ARGUMENT_SYMBOL) {
                    addToken(TokenType.END_SUBEXPRESSION);
                    position++;
                } else {
                    addToken(TokenType.END_ARGUMENT);
                }
            } else {
                addToken(tokenTypeBySymbol.get(current));
            }
        } else {
            buffer.append(current);
        }

        position++;

    }


    private void addToken(TokenType type) {
        tokens.add(new Token(type,tokenStart));
        tokenStart++;
    }


    private boolean hasNext() {
        return position < text.length();
    }


    private void dumpBuffer() {
        String value = buffer.toString();
        buffer.delete(0, buffer.length());
        if (!value.isBlank()) {
            tokens.add(new Token(TokenType.TEXT, value, tokenStart, position));
            tokenStart = position + 1;
        }
    }


    private void abort(String message) {
        throw new ExpressionException(text,position,message);
    }


}
