package de.jm.tsfto.model.song;

import de.jm.tsfto.parser.SymbolParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.jm.tsfto.model.song.KeyValueLine.*;

public class SymbolLine extends SongLine {

    private static final Map<String, String> symbolToLatex = new HashMap<>();

    static {
        symbolToLatex.put("cresc", "\\cresc");
        symbolToLatex.put("decresc", "\\decresc");
        symbolToLatex.put("refrain", "\\refrain");
        symbolToLatex.put("solo", "\\solo");
        symbolToLatex.put("^", "\\fer");
        symbolToLatex.put("%", "\\rse");
        symbolToLatex.put("$", "\\coda");
        symbolToLatex.put("ds", "\\rds");
        symbolToLatex.put("dc", "\\rdc");

        symbolToLatex.put("DS", "\\DS");
        symbolToLatex.put("DC", "\\DC");
        symbolToLatex.put("1.", "\\first");
        symbolToLatex.put("2.", "\\second");

        symbolToLatex.put("f", "\\lf");
        symbolToLatex.put("mf", "\\lmf");
        symbolToLatex.put("ff", "\\lff");
        symbolToLatex.put("p", "\\lp");
        symbolToLatex.put("pp", "\\lpp");
        symbolToLatex.put("ppp", "\\lppp");
        symbolToLatex.put("mp", "\\lmp");

        symbolToLatex.put(">", "\\hfill");
        symbolToLatex.put("_", " ");
        symbolToLatex.put("*", "&");
        symbolToLatex.put("!", "\\ms");
        symbolToLatex.put("|", "\\ms");
        symbolToLatex.put("!!", "\\ds");
        symbolToLatex.put("||", "\\ds");
    }

    private static final Map<String, String> keyValueToLatex = new HashMap<>();

    static {
        keyValueToLatex.put("b:", "\\mnbr{%s}");
        keyValueToLatex.put("p:", "\\tpart{%s}");
        keyValueToLatex.put("key:", "\\key{%s}");
    }


    public SymbolLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        if (line == null || line.isEmpty()) {
            return "";
        }

        StringBuilder latexBuilder = new StringBuilder();
        List<String> tokens = List.of(line.split(" "));

        for (String token : tokens) {
            int cols = getColCount(token);
            String aligned = isRightAligned(token) ? "R" : "L";
            String processedToken = processToken(token);
            processedToken = fillSymbols(processedToken);
            if (processedToken.isEmpty()) {
                latexBuilder.append("&".repeat(cols));
            } else {
                if (!latexBuilder.isEmpty() && latexBuilder.charAt(latexBuilder.length() - 1) != '&') {
                    latexBuilder.append("&");
                }
                if (cols == 1) {
                    latexBuilder.append(processedToken).append("&");
                } else {
                    latexBuilder.append("\\multicolumn{%s}{%s}{%s}".formatted(cols, aligned, processedToken)).append("&");
                }
            }
        }
        return latexBuilder.toString();
    }

    static String processToken(String token) {
        String processToken = token.replaceAll("^\\*+", "");
        processToken = processToken.replaceAll("\\*+$", "");
        processToken = processToken.replace('*', '_');
        processToken = processToken.replaceAll(" \\+", " ");
        return processToken;
    }

    static String fillSymbols(String token) {
        StringBuilder latexBuilder = new StringBuilder();
        List<String> parts = SymbolParser.parse(token);
        for (String part : parts) {
            if (symbolToLatex.containsKey(part)) {
                latexBuilder.append(symbolToLatex.get(part));
            } else {
                if (isKeyValue(part)) {
                    String key = getKey(part) + ":";
                    if (keyValueToLatex.containsKey(key)) {
                        latexBuilder.append(keyValueToLatex.get(key).formatted(getValue(part)));
                    }
                }
                else {
                    latexBuilder.append("\\sign{%s}".formatted(part));
                }
            }
        }

        return latexBuilder.toString();
    }

    static String[] knownKeyValues = {"b:", "p:", "key:"};

    static List<String> getKeyValues(String processedToken) {
        List<String> keyValues = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String knownKeyValue : knownKeyValues) {
            int posKey = processedToken.indexOf(knownKeyValue);
            if (posKey != -1) {
                for (int pos = posKey + knownKeyValue.length(); pos < processedToken.length(); pos++) {
                    char c = processedToken.charAt(pos);
                    if (c == '_' || c == ' ') {
                        break;
                    } else {
                        sb.append(c);
                    }
                }
                keyValues.add(knownKeyValue + sb);
                sb = new StringBuilder();
            }
        }
        return keyValues;
    }

    static int getColCount(String token) {
        int count = 0;
        for (char c : token.toCharArray()) {
            if (c == '*') {
                count++;
            }
        }
        token = token.replace('*', ' ').trim();
        token = token.replaceAll(" \\+", " ");
        String[] parts = token.split(" ");
        for (String part : parts) {
            if (!part.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    static boolean isRightAligned(String token) {
        return token.startsWith("*");
    }

    public static boolean matches(String line) {
        return line.startsWith("s:");
    }
}
