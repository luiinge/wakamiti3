package es.iti.wakamiti.plugins.gherkin.parser.internal;

import java.util.*;


public class AstNode {

    private final Map<RuleType, List<Object>> subItems = new EnumMap<>(RuleType.class);
    public final RuleType ruleType;

    public AstNode(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public void add(RuleType ruleType, Object obj) {
        subItems.computeIfAbsent(ruleType, it->new ArrayList<>()).add(obj);
    }


    @SuppressWarnings("unchecked")
    public <T> T getSingle(RuleType ruleType, T defaultResult) {
        List<Object> items = getItems(ruleType);
        return (T) (items.isEmpty() ? defaultResult : items.get(0));
    }


    public <T> T getSingle(RuleType ruleType) {
        return getSingle(ruleType, null);
    }


    @SuppressWarnings("unchecked")
    public <T> List<T> getItems(RuleType ruleType) {
        List<T> items = (List<T>) subItems.get(ruleType);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    public Token getToken(TokenType tokenType) {
        RuleType tokenRuleType = RuleType.cast(tokenType);
        return getSingle(tokenRuleType, new Token(null, null));
    }

    public List<Token> getTokens(TokenType tokenType) {
        return getItems(RuleType.cast(tokenType));
    }
}
