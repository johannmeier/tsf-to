package de.jm.tsfto.model.song;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolLine extends SongLine {

    private static Map<String, String> symbolToLatex = new HashMap<>();
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
            String aligned = isRightAligned(token) ? "r" : "l";
            String processedToken = processToken(token);
            processedToken = fillSymbols(processedToken);
            if (processedToken.isEmpty()) {
                latexBuilder.append("&".repeat(cols));
            } else {
                if (!latexBuilder.isEmpty() && latexBuilder.charAt(latexBuilder.length() - 1) != '&') {
                    latexBuilder.append("&");
                }
                if (cols == 1) {
                    latexBuilder.append(processedToken);
                } else {
                    latexBuilder.append("\\multicolumn{%s}{%s}{%s}".formatted(processedToken, cols, aligned));
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
        String processedToken = token;
        for (String symbol : symbolToLatex.keySet()) {
            processedToken = processedToken.replace(symbol, symbolToLatex.get(symbol));
        }
        return processedToken;
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
