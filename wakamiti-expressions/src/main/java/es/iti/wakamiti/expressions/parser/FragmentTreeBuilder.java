package es.iti.wakamiti.expressions.parser;

import java.util.*;

import es.iti.wakamiti.expressions.ExpressionException;
import jexten.plugin.PluginException;


public class FragmentTreeBuilder {

    private enum State {
        DEFAULT,
        NEGATION,
        NEGATION_GROUP,
        OPTIONAL,
        OPTIONAL_CHOICE,
        WORD_CHOICE,
        GROUP,
        GROUP_CHOICE,
        ARGUMENT,
        SUBEXPRESSION
    }

    private final Iterator<Token> tokens;
    private final Deque<State> stateStack = new LinkedList<>();
    private final Deque<FragmentTree> nodeStack = new LinkedList<>();

    private Token currentToken;
    private Token previousToken;




    FragmentTreeBuilder(Iterator<Token> tokens) {
        this.tokens = tokens;
    }


    FragmentTreeBuilder(String text) {
        this(new Tokenizer(text).tokens().iterator());
    }




    public FragmentTree buildTree() {

        pushState(State.DEFAULT, FragmentTree.Type.SEQUENCE.empty());

        while (tokens.hasNext()) {
            previousToken = currentToken;
            currentToken = tokens.next();
            processToken();

        }

        if (stateStack.getLast() != State.DEFAULT) {
            throw new PluginException("unexpected final state "+stateStack.getLast());
        }
        return nodeStack.getLast().reduced();

    }


    private void processToken() {
        switch (stateStack.getLast()) {
            case DEFAULT -> processDefaultState();
            case NEGATION -> processNegationState();
            case NEGATION_GROUP -> processNegationGroupState();
            case OPTIONAL -> processOptionalState();
            case OPTIONAL_CHOICE -> processOptionalChoiceState();
            case GROUP -> processGroupState();
            case GROUP_CHOICE -> processGroupChoiceState();
            case WORD_CHOICE -> processWordChoiceState();
            case ARGUMENT -> processArgumentState();
            case SUBEXPRESSION -> processSubexpressionState();
            default -> abort(currentToken);
        }
    }


    private void processDefaultState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.SEQUENCE);
        switch (currentToken.type()) {
            case TEXT -> parentNode.add(FragmentTree.Type.LITERAL.of(currentToken));
            case WILDCARD -> parentNode.add(FragmentTree.Type.WILDCARD.empty());
            case NEGATION -> pushState(State.NEGATION, FragmentTree.Type.NEGATION);
            case START_OPTIONAL -> pushState(State.OPTIONAL, FragmentTree.Type.OPTIONAL);
            case START_GROUP -> pushState(State.GROUP, FragmentTree.Type.SEQUENCE);
            case START_ARGUMENT -> pushState(State.ARGUMENT, FragmentTree.Type.ARGUMENT);
            case START_SUBEXPRESSION -> pushState(State.SUBEXPRESSION, FragmentTree.Type.SUBEXPRESSION);
            case CHOICE_SEPARATOR -> processDefaultChoiceSeparator();
            default -> abort(currentToken);
        }
    }


    private void processDefaultChoiceSeparator() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.SEQUENCE);
        assertToken(previousToken, TokenType.TEXT);
        if (previousToken.endsWithBlank()) {
            abort(currentToken);
        }
        FragmentTree lastNode = parentNode.lastChild();
        parentNode.remove(lastNode);
        if (previousToken.isSingleWord()) {
            pushState(State.WORD_CHOICE, FragmentTree.Type.CHOICE.of(lastNode));
        } else {
            String lastWord = previousToken.lastWord();
            parentNode.add(
                FragmentTree.Type.LITERAL.of(previousToken.removeTrailingChars(lastWord.length()))
            );
            pushState(State.WORD_CHOICE, FragmentTree.Type.CHOICE.of(FragmentTree.Type.LITERAL.of(lastWord)));
        }
    }


    private void processNegationState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.NEGATION);
        switch (currentToken.type()) {
            case TEXT -> {
                if (currentToken.startsWithBlank()) {
                    abort(currentToken);
                }
                if (currentToken.isSingleWord()) {
                    parentNode.add(FragmentTree.Type.LITERAL.of(currentToken));
                    popState();
                } else {
                    String firstWord = currentToken.firstWord();
                    parentNode.add(FragmentTree.Type.LITERAL.of(firstWord));
                    popState();
                    previousToken = currentToken.firstWordToken();
                    currentToken = currentToken.removeLeadingChars(firstWord.length());
                    processToken();
                }
            }
            case START_GROUP -> mutateState(State.NEGATION_GROUP);
            default -> abort(currentToken);
        }
    }


    private void processNegationGroupState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.NEGATION);
        switch (currentToken.type()) {
            case TEXT -> { }
            case END_GROUP -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
                popState();
            }
            default -> abort(currentToken);
        }
    }


    private void processOptionalState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.OPTIONAL);
        switch (currentToken.type()) {
            case TEXT -> { }
            case END_OPTIONAL -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
                popState();
            }
            case CHOICE_SEPARATOR -> {
                assertToken(previousToken, TokenType.TEXT);
                pushState(State.OPTIONAL_CHOICE, FragmentTree.Type.CHOICE);
                FragmentTree choiceNode = nodeStack.getLast();
                choiceNode.add(FragmentTree.Type.LITERAL.of(previousToken));
            }
            default -> abort(currentToken);
        }
    }


    private void processOptionalChoiceState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.CHOICE);
        switch (currentToken.type()) {
            case TEXT -> { }
            case CHOICE_SEPARATOR -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
            }
            case END_OPTIONAL -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
                popState();
                popState();
            }
            default -> abort(currentToken);
        }
    }


    private void processGroupState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.SEQUENCE);
        switch (currentToken.type()) {
            case TEXT -> { }
            case END_GROUP -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
                popState();
            }
            case CHOICE_SEPARATOR -> {
                assertToken(previousToken, TokenType.TEXT);
                pushState(State.GROUP_CHOICE, FragmentTree.Type.CHOICE);
                FragmentTree choiceNode = nodeStack.getLast();
                choiceNode.add(FragmentTree.Type.LITERAL.of(previousToken));
            }
            default -> abort(currentToken);
        }
    }


    private void processGroupChoiceState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.CHOICE);
        switch (currentToken.type()) {
            case TEXT -> { }
            case CHOICE_SEPARATOR -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
            }
            case END_GROUP -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
                popState();
                popState();
            }
            default -> abort(currentToken);
        }
    }


    private void processWordChoiceState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.CHOICE);
        switch (currentToken.type()) {
            case TEXT -> {
                if (currentToken.startsWithBlank()) {
                    abort(currentToken);
                }
                if (!currentToken.isSingleWord()) {
                    String firstWord = currentToken.firstWord();
                    parentNode.add(FragmentTree.Type.LITERAL.of(firstWord));
                    popState();
                    previousToken = currentToken.firstWordToken();
                    currentToken = currentToken.removeLeadingChars(firstWord.length());
                    processToken();
                }
            }
            case CHOICE_SEPARATOR -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.add(FragmentTree.Type.LITERAL.of(previousToken));
            }
            default -> abort(currentToken);
        }
    }



    private void processArgumentState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.ARGUMENT);
        switch (currentToken.type()) {
            case TEXT -> {
                if (!currentToken.isSingleWord()) {
                    abort(currentToken);
                }
            }
            case END_ARGUMENT -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.value = previousToken.value();
                popState();
            }
            default -> abort(currentToken);
        }
    }


    private void processSubexpressionState() {
        var parentNode = nodeStack.getLast().assertType(FragmentTree.Type.SUBEXPRESSION);
        switch (currentToken.type()) {
            case TEXT -> {
                if (!currentToken.isSingleWord()) {
                    abort(currentToken);
                }
            }
            case END_SUBEXPRESSION -> {
                assertToken(previousToken, TokenType.TEXT);
                parentNode.value = previousToken.value();
                popState();
            }
            default -> abort(currentToken);
        }
    }

    private void assertToken(Token token, TokenType type) {
        if (token == null || token.type() != type) {
            abort(currentToken);
        }
    }


    private void pushState(State newState, FragmentTree.Type newNodeType) {
        pushState(newState, newNodeType.empty());
    }


    private void pushState(State newState, FragmentTree newNode) {
        this.stateStack.addLast(newState);
        this.nodeStack.addLast(newNode);
    }


    private void mutateState(State newState) {
        this.stateStack.removeLast();
        this.stateStack.addLast(newState);
    }


    private void popState() {
        this.stateStack.removeLast();
        var currentNode = this.nodeStack.removeLast();
        this.nodeStack.getLast().add(currentNode);
    }


    private void abort(Token currentToken) {
        throw new ExpressionException("Unexpected token {}", currentToken);
    }
    
}
