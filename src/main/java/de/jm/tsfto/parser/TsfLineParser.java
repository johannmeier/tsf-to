package de.jm.tsfto.parser;


import de.jm.tsfto.model.tsf.TsfToken;

import java.util.ArrayList;
import java.util.List;

public class TsfLineParser {
    public static final String TOKEN_START = "!;:.,/";
    public static final String TOKEN_NOTES = "drmfsltb-";
    public static final String TOKEN_REST = "!;:.,/";

    private final String line;
    private int pos = 0;

    public TsfLineParser(String line) {
        this.line = line;
    }

    public List<TsfToken> parse() {

        List<TsfToken> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        for (pos = 0; pos < line.length(); pos++) {
            if (isStartToken() && !token.isEmpty()) {
                tokens.add(TsfToken.of(token.toString().trim()));
                token.setLength(0);
                if (isHalfQuarter() || isThirdThird() || isHalfHalfQuarter() || isDoubleBar()) {
                    token.append(current());
                    pos++;
                }
            }
            token.append(current());
        }
        if (!token.isEmpty()) {
            tokens.add(TsfToken.of(token.toString().trim()));
        }
        return tokens;
    }

    private boolean isStartToken() {
        if (isHalfQuarter() || isThirdThird() || isHalfHalfQuarter() || isDoubleBar())
            return true;
        if (isSingleRestOrNote()) {
            return true;
        } else {
            return TOKEN_START.contains(current()) && TOKEN_NOTES.contains(next());
        }
    }

    private boolean isHalfQuarter() {
        return ".".equals(current()) && ",".equals(next());
    }

    private boolean isHalfHalfQuarter() {
        return ",".equals(current()) && ",".equals(next());
    }

    private boolean isThirdThird() {
        return "/".equals(current()) && "/".equals(next());
    }

    private boolean isDoubleBar() {
        return "!".equals(current()) && "!".equals(next()) || "|".equals(current()) && "|".equals(next());
    }

    public static boolean isEndToken(String token) {
        return "!!".equals(token) || "||".equals(token);
    }

    private boolean isSingleRestOrNote() {
        return " ".equals(previous())
                && (TOKEN_REST.contains(current()) || TOKEN_NOTES.contains(current()) || "*".equals(current()))
                && (" ".equals(next()) || next().isEmpty());
    }

    private String current() {
        return String.valueOf(line.charAt(pos));
    }

    private String next() {
        if (pos + 1 < line.length()) {
            return String.valueOf(line.charAt(pos + 1));
        } else {
            return "";
        }
    }

    private String previous() {
        if (pos > 0) {
            return String.valueOf(line.charAt(pos - 1));
        } else {
            return "";
        }
    }
}
