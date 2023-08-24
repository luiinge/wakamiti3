package es.iti.wakamiti.expressions.parser;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;



class TestTokenizer {


    @Test
    void tokenize() {
        var tokens = new Tokenizer("""
           This is a simple expression, with some symbols (like these), \
           or [these]. ^Negated (words|phrases), {{subexpressions}} and also \
           arguments like {number}. Escaped symbols are: \\^\\(\\)\\[\\]\\{\\}\\|\\\\.
           """).tokens();
        System.out.println(tokens);
        assertThat(tokens).containsExactly(
            new Token(TokenType.TEXT, "This is a simple expression, with some symbols ", 0, 47),
            new Token(TokenType.START_OPTIONAL,  48),
            new Token(TokenType.TEXT, "like these", 49, 58),
            new Token(TokenType.END_OPTIONAL,  59),
            new Token(TokenType.TEXT, ", or ", 60, 64),
            new Token(TokenType.START_GROUP,  65),
            new Token(TokenType.TEXT, "these", 66, 70),
            new Token(TokenType.END_GROUP,  71, 71),
            new Token(TokenType.TEXT, ". ", 72, 73),
            new Token(TokenType.NEGATION,  74),
            new Token(TokenType.TEXT, "Negated ", 75, 82),
            new Token(TokenType.START_OPTIONAL,  83, 83),
            new Token(TokenType.TEXT, "words", 84, 88),
            new Token(TokenType.CHOICE_SEPARATOR,  89),
            new Token(TokenType.TEXT, "phrases", 90, 96),
            new Token(TokenType.END_OPTIONAL,  97),
            new Token(TokenType.TEXT, ", ", 98, 99),
            new Token(TokenType.START_SUBEXPRESSION,  100),
            new Token(TokenType.TEXT, "subexpressions", 101, 115),
            new Token(TokenType.END_SUBEXPRESSION,  116),
            new Token(TokenType.TEXT, " and also arguments like ", 117, 142),
            new Token(TokenType.START_ARGUMENT,  143),
            new Token(TokenType.TEXT, "number", 144, 149),
            new Token(TokenType.END_ARGUMENT,  150),
            new Token(TokenType.TEXT, ". Escaped symbols are: ^()[]{}|\\.", 151, 192)
        );
    }


   

}
