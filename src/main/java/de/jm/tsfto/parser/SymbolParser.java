package de.jm.tsfto.parser;

import java.util.ArrayList;
import java.util.List;

public class SymbolParser {

    private final String symbolsToken;
    private int pos;

    SymbolParser(String symbolsToken) {
        this.symbolsToken = symbolsToken;
    }

    public static List<String> parse(String symbol) {
        return new SymbolParser(symbol).parse();
    }

    private List<String> parse() {
        List<String> tokens = new ArrayList<>();
        while (hasNext()) {
            tokens.add(nextToken());
        }
        return tokens;
    }

    private static boolean isSingleToken(Character c) {
        String startTokens = "_$^%*";
        return c != null && startTokens.contains("" + c);
    }

    private static boolean isTokenStart(Character c) {
        return c != null && (isSingleToken(c) || c == '>' || c == '<');
    }

    boolean hasNext() {
        return pos < symbolsToken.length();
    }

    String nextToken() {
        Character current = currentChar();
        pos++;
        StringBuilder token = new StringBuilder();
        token.append(current);

        if (isSingleToken(current)) {
            return token.toString();
        }

        if (current == '>' || current == '<') {
            if (currentChar() == null || !Character.isDigit(currentChar())) {
                return token.toString();
            }
        }

        while ((current = currentChar()) != null) {
            if (isTokenStart(current)) {
                break;
            }
            token.append(current);
            pos++;
        }
        return token.toString();
    }

    Character currentChar() {
        if (pos < symbolsToken.length()) {
            return symbolsToken.charAt(pos);
        }
        return null;
    }
}
