package de.jm.tsfto.parser;

import java.util.ArrayList;
import java.util.List;

public class SymbolParser {

    private final String symbolsToken;
    private int pos;

    private SymbolParser(String symbolsToken) {
        this.symbolsToken = symbolsToken;
    }

    public static List<String> parse(String symbol) {
        return new SymbolParser(symbol).parse();
    }

    private List<String> parse() {
        List<String> tokens = new ArrayList<>();

        StringBuilder token = new StringBuilder();
        for (pos = 0; pos < symbolsToken.length(); pos++) {
            String current = current();

            if (isSingleToken(current)) {
                tokens.add(current);
                continue;
            }

            token.append(current);

            String next = next();

            if (next == null) {
                tokens.add(token.toString());
            }

            if (isSingleToken(next)) {
                tokens.add(token.toString());
                token.setLength(0);

                tokens.add(next);
                pos++;
            }
        }

        return tokens;
    }

    private boolean isSingleToken(String next) {
        String startTokens = "_>$^%*";
        return next != null && startTokens.contains(next);
    }

    String current() {
        return String.valueOf(symbolsToken.charAt(pos));
    }

    String next() {
        if (pos + 1 < symbolsToken.length()) {
            return String.valueOf(symbolsToken.charAt(pos + 1));
        } else {
            return null;
        }
    }
}
