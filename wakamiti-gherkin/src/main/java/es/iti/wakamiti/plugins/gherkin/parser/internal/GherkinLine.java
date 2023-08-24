package es.iti.wakamiti.plugins.gherkin.parser.internal;

import java.util.*;

import es.iti.wakamiti.plugins.gherkin.parser.GherkinLanguageConstants;

public class GherkinLine {

    private final String lineText;
    private final String trimmedLineText;

    public GherkinLine(String lineText) {
        this.lineText = lineText;
        this.trimmedLineText = lineText.stripLeading();
    }

    public Integer indent() {
        return countSymbols(lineText) - countSymbols(trimmedLineText);
    }


    public String getLineText(int indentToRemove) {
        if (indentToRemove < 0 || indentToRemove > indent())
            return trimmedLineText;
        return lineText.substring(indentToRemove);
    }

    public boolean isEmpty() {
        return trimmedLineText.length() == 0;
    }

    public boolean startsWith(String prefix) {
        return trimmedLineText.startsWith(prefix);
    }

    public String getRestTrimmed(int length) {
        return trimmedLineText.substring(length).trim();
    }

    public List<GherkinLineSpan> getTags() {
        return getSpans();
    }

    public boolean startsWithTitleKeyword(String text) {
        int textLength = text.length();
        return trimmedLineText.length() > textLength &&
                trimmedLineText.startsWith(text) &&
                trimmedLineText.substring(textLength, textLength + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length())
                        .equals(GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR);
    }


    public List<GherkinLineSpan> getTableCells() {

        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean beforeFirst = true;
        int startCol = 0;
        int col = 0;

        while(col < trimmedLineText.length()) {

            char c = trimmedLineText.charAt(col);

            if (c == '|') {

                if (beforeFirst) {
                    // Skip the first empty span
                    beforeFirst = false;
                    cell = new StringBuilder();
                    col++;
                    continue;
                }

                int contentStart = 0;
                while (contentStart < cell.length() && Character.isWhitespace(cell.charAt(contentStart))) {
                    contentStart++;
                }
                if (contentStart == cell.length()) {
                    contentStart = 0;
                }
                lineSpans.add(new GherkinLineSpan(indent() + startCol + contentStart + 2, cell.toString().trim()));
                startCol = col;
                cell = new StringBuilder();

            } else if (c == '\\') {

                col++;
                c = trimmedLineText.charAt(col);
                if (c == 'n') {
                    cell.append('\n');
                } else {
                    if (c != '|' && c != '\\') {
                        cell.append('\\');
                    }
                    cell.append(c);
                }
            } else {
                cell.append(c);
            }
            col++;
        }

        return lineSpans;
    }



    private List<GherkinLineSpan> getSpans() {
        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        try(Scanner scanner = new Scanner(trimmedLineText)) {
            scanner.useDelimiter("\\s+");
            while (scanner.hasNext()) {
                String cell = scanner.next();
                int column = scanner.match().start() + indent() + 1;
                lineSpans.add(new GherkinLineSpan(column, cell));
            }
            return lineSpans;
            }
    }


    private static int countSymbols(String text) {
        return text.codePointCount(0, text.length());
    }
}
