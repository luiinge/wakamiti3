package es.iti.wakamiti.expressions.parser;

import static es.iti.wakamiti.expressions.parser.FragmentTree.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import es.iti.wakamiti.api.*;


class TestExpressionBuilder {



    static ExpressionBuilder builder = new ExpressionBuilder(
        Locale.ENGLISH,
        DataTypes.of(),
        SubExpressions.of()
    );




    static Stream<Arguments> simpleExpressions() {
        return Stream.of(
            Arguments.of(
                "this is a simple expression",
                "(this\\s+is\\s+a\\s+simple\\s+expression)"
            ),
            Arguments.of(
                "this is an \\^escaped \\(expression",
                "(this\\s+is\\s+an\\s+\\^escaped\\s+\\(expression)"
            ),
            Arguments.of(
                "this is a ^negated word",
                "(this\\s+is\\s+a\\s+)(?!(negated))\\S+(\\s+word)"
            ),
            Arguments.of(
                "this is a ^[negated phrase]",
                "(this\\s+is\\s+a\\s+)(?!(negated\\s+phrase)).*"
            ),
            Arguments.of(
                "this is an (optional) word",
                "(this\\s+is\\s+an\\s+)(optional\\s+)?(word)"
            ),
            Arguments.of(
                "this is an (optional phrase)",
                "(this\\s+is\\s+an\\s+)(optional\\s+phrase)?"
            ),
            Arguments.of(
                "this is a word1|word2 choice",
                "this is a (word1)|(word2) choice"
            ),
            Arguments.of(
                "this is a [one phrase|another phrase] choice",""
            ),
            Arguments.of(
                "this is an optional (word1 | word2) choice",""
            ),
            Arguments.of(
                "this is an option(al) suffix",""
            ),
            Arguments.of(
                "this is an optional ( one phrase| another phrase) choice",""
            ),
            Arguments.of(
                "this is a wildcard: *",""
            )
        );
    }


    @ParameterizedTest
    @MethodSource("simpleExpressions")
    void simpleExpression(String expression, String regex) {
        var fragments = builder.buildExpression(expression).fragments();
        System.out.println(fragments);
        assertThat(fragments).hasSize(1);
        var fragment = (RegexFragment) fragments.get(0);
        assertThat(fragment.literal()).isEqualTo(expression);
        assertThat(fragment.pattern().pattern()).isEqualTo(regex);
    }





    @Test
    void arguments() {
        var tree = new FragmentTreeBuilder("""
            this is an unnamed argument {number} and a named argument {name:text}
        """).buildTree();
        System.out.println(tree);
        assertThat(tree).isEqualTo(
            SEQUENCE.of(
                LITERAL.of("this is an unnamed argument "),
                ARGUMENT.of("number"),
                LITERAL.of(" and a named argument "),
                ARGUMENT.of("name:text")
            )
        );
    }


}
