/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.plugins.gherkin.parser.internal;


import java.util.*;
import java.util.stream.*;

import es.iti.wakamiti.plugins.gherkin.parser.elements.*;


public class GherkinAstBuilder  {

    private Deque<AstNode> stack;
    private List<Comment> currentComments;
    private final Map<Token, List<Comment>> comments = new HashMap<>();


    public GherkinAstBuilder() {
        reset();
    }


    
    public void reset() {
        stack = new ArrayDeque<>();
        stack.push(new AstNode(RuleType.NONE));
        currentComments = new ArrayList<>();
        comments.clear();
    }


    private AstNode currentNode() {
        return stack.peek();
    }


    
    public void build(Token token) {
        RuleType ruleType = RuleType.cast(token.matchedType());
        switch (token.matchedType()) {
            case COMMENT -> currentComments.add(new Comment(location(token, 0), token.matchedText()));
            case FEATURE_LINE, SCENARIO_LINE, SCENARIO_OUTLINE_LINE, BACKGROUND_LINE, STEP_LINE -> {
                comments.put(token, currentComments);
                currentComments = new ArrayList<>();
                currentNode().add(ruleType, token);
            }
            default -> currentNode().add(ruleType, token);
        }
    }


    
    public void startRule(RuleType ruleType) {
        stack.push(new AstNode(ruleType));
    }


    
    public void endRule() {
        AstNode node = stack.pop();
        Object transformedNode = getTransformedNode(node);
        currentNode().add(node.ruleType, transformedNode);
    }


    private Object getTransformedNode(AstNode node) {
        return switch (node.ruleType) {
        case STEP ->
            getTransformedStep(node);
        case DOC_STRING ->
            getTransformedDocString(node);
        case DATA_TABLE ->
            new DataTable(getTableRows(node));
        case BACKGROUND ->
            getTransformedBackground(node);
        case SCENARIO_DEFINITION ->
            getTransformedScenarioDefinition(node);
        case EXAMPLES_DEFINITION ->
            getTransformedExamplesDefinition(node);
        case EXAMPLES_TABLE ->
            getTableRows(node);
        case DESCRIPTION ->
            getTransformedDescription(node);
        case FEATURE ->
            getTransformedFeature(node);
        case GHERKIN_DOCUMENT ->
            new GherkinDocument(node.getSingle(RuleType.FEATURE));
        default ->
            node;
        };
    }


    private Object getTransformedStep(AstNode node) {
        Token stepLine = node.getToken(TokenType.STEP_LINE);
        return new Step(
            location(stepLine, 0),
            comments(node),
            stepLine.matchedKeyword(),
            stepLine.matchedText(),
            stepArgument(node).orElse(null)
        );
    }


    private Optional<StepArgument> stepArgument(AstNode node) {
        DataTable dataTable = node.getSingle(RuleType.DATA_TABLE);
        DocString docString = node.getSingle(RuleType.DOC_STRING);
        return Optional.ofNullable(dataTable != null ? dataTable : docString);
    }


    private Object getTransformedDocString(AstNode node) {
        Token separatorToken = node.getTokens(TokenType.DOC_STRING_SEPARATOR).get(0);
        String contentType =
            (separatorToken.matchedText().length() > 0 ? separatorToken.matchedText() : null);
        List<Token> lineTokens = node.getTokens(TokenType.OTHER);
        StringBuilder content = new StringBuilder();
        boolean newLine = false;
        for (Token lineToken : lineTokens) {
            if (newLine) {
                content.append("\n");
            }
            newLine = true;
            content.append(lineToken.matchedText());
        }
        return new DocString(
            location(separatorToken, 0),
            contentType,
            content.toString()
        );
    }


    private Object getTransformedBackground(AstNode node) {
        Token backgroundLine = node.getToken(TokenType.BACKGROUND_LINE);
        return new Background(
            location(backgroundLine, 0),
            comments(node),
            List.of(),
            backgroundLine.matchedKeyword(),
            backgroundLine.matchedText(),
            getDescription(node),
            getSteps(node)
        );
    }


    private Object getTransformedScenarioDefinition(AstNode node) {
        List<Tag> tags = getTags(node);
        AstNode scenarioNode = node.getSingle(RuleType.SCENARIO, null);

        if (scenarioNode != null) {

            Token scenarioLine = scenarioNode.getToken(TokenType.SCENARIO_LINE);
            String description = getDescription(scenarioNode);
            List<Step> steps = getSteps(scenarioNode);
            return new Scenario(
                location(scenarioLine, 0),
                comments(scenarioNode),
                tags,
                scenarioLine.matchedKeyword(),
                scenarioLine.matchedText(),
                description,
                steps
            );

        } else {
            AstNode scenarioOutlineNode = node.getSingle(RuleType.SCENARIO_OUTLINE, null);
            if (scenarioOutlineNode == null) {
                throw new IllegalArgumentException("Internal grammar error");
            }
            Token scenarioOutlineLine = scenarioOutlineNode.getToken(TokenType.SCENARIO_OUTLINE_LINE);
            String description = getDescription(scenarioOutlineNode);
            List<Step> steps = getSteps(scenarioOutlineNode);

            List<Examples> examplesList = scenarioOutlineNode
                .getItems(RuleType.EXAMPLES_DEFINITION);

            return new ScenarioOutline(
                location(scenarioOutlineLine, 0),
                comments(scenarioOutlineNode),
                tags,
                scenarioOutlineLine.matchedKeyword(),
                scenarioOutlineLine.matchedText(),
                description,
                steps,
                examplesList
            );
        }
    }


    private Object getTransformedExamplesDefinition(AstNode node) {
        List<Tag> tags = getTags(node);
        AstNode examplesNode = node.getSingle(RuleType.EXAMPLES, null);
        Token examplesLine = examplesNode.getToken(TokenType.EXAMPLES_LINE);
        String description = getDescription(examplesNode);
        List<TableRow> rows = examplesNode.getSingle(RuleType.EXAMPLES_TABLE, null);
        TableRow tableHeader =
            (rows != null && !rows.isEmpty() ? rows.get(0) : null);
        List<TableRow> tableBody =
            (rows != null && !rows.isEmpty() ? rows.subList(1, rows.size()) : List.of());
        return new Examples(
            location(examplesLine, 0),
            List.of(),
            tags,
            examplesLine.matchedKeyword(),
            examplesLine.matchedText(),
            description,
            tableHeader,
            tableBody
        );
    }


    private Object getTransformedDescription(AstNode node) {
        List<Token> lineTokens = node.getTokens(TokenType.OTHER);
        // Trim trailing empty lines
        int end = lineTokens.size();
        while (end > 0 && lineTokens.get(end - 1).matchedText().matches("\\s*")) {
            end--;
        }
        lineTokens = lineTokens.subList(0, end);
        return lineTokens.stream().map(Token::matchedText).collect(Collectors.joining("\n"));
    }


    private Object getTransformedFeature(AstNode node) {
        AstNode header = node
            .getSingle(RuleType.FEATURE_HEADER, new AstNode(RuleType.FEATURE_HEADER));
        if (header == null) {
            return null;
        }
        List<Tag> tags = getTags(header);
        Token featureLine = header.getToken(TokenType.FEATURE_LINE);
        if (featureLine == null) {
            return null;
        }
        List<ScenarioDefinition> scenarioDefinitions = new ArrayList<>();
        Background background = node.getSingle(RuleType.BACKGROUND, null);
        if (background != null) {
            scenarioDefinitions.add(background);
        }
        scenarioDefinitions.addAll(node.getItems(RuleType.SCENARIO_DEFINITION));
        String description = getDescription(header);
        if (featureLine.matchedGherkinDialect() == null) {
            return null;
        }
        String language = featureLine.matchedGherkinDialect().language();

        return new Feature(
            location(featureLine, 0),
            comments(header),
            tags,
            featureLine.matchedKeyword(),
            featureLine.matchedText(),
            description,
            scenarioDefinitions,
            language
        );
    }


    private List<TableRow> getTableRows(AstNode node) {
        List<TableRow> rows = new ArrayList<>();
        for (Token token : node.getTokens(TokenType.TABLE_ROW)) {
            rows.add(new TableRow(location(token, 0), cells(token)));
        }
        ensureCellCount(rows);
        return rows;
    }


    private void ensureCellCount(List<TableRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        int cellCount = rows.get(0).cells().size();
        for (TableRow row : rows) {
            if (row.cells().size() != cellCount) {
                throw new AstBuilderException(
                    "inconsistent cell count within the table", row.location()
                );
            }
        }
    }


    private List<TableCell> cells(Token token) {
        List<TableCell> cells = new ArrayList<>();
        for (GherkinLineSpan cellItem : token.matchedItems()) {
            cells.add(new TableCell(location(token, cellItem.column), cellItem.text));
        }
        return cells;
    }


    private List<Step> getSteps(AstNode node) {
        return node.getItems(RuleType.STEP);
    }


    private Location location(Token token, int column) {
        return column == 0 ? token.location() : new Location(token.location().line(), column);
    }


    private String getDescription(AstNode node) {
        return node.getSingle(RuleType.DESCRIPTION, "").strip();
    }


    private List<Tag> getTags(AstNode node) {
        AstNode tagsNode = node.getSingle(RuleType.TAGS, new AstNode(RuleType.NONE));
        if (tagsNode == null) {
            return new ArrayList<>();
        }

        List<Token> tokens = tagsNode.getTokens(TokenType.TAG_LINE);
        List<Tag> tags = new ArrayList<>();
        for (Token token : tokens) {
            for (GherkinLineSpan tagItem : token.matchedItems()) {
                tags.add(new Tag(location(token, tagItem.column), tagItem.text));
            }
        }
        return tags;
    }


    
    public GherkinDocument getResult() {
        return currentNode().getSingle(RuleType.GHERKIN_DOCUMENT, null);
    }


    private List<Comment> comments(AstNode node) {
        List<Token> tokens = Stream.of(TokenType.values()).map(node::getTokens)
            .collect(ArrayList::new, List::addAll, List::addAll);
        return tokens.stream().map(comments::get).filter(Objects::nonNull)
            .collect(ArrayList::new, List::addAll, List::addAll);
    }

}
