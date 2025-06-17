package de.jm.tsfto.model.song;

import java.util.List;

public class TextLine extends SongLine {

    public TextLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        StringBuilder latexBuilder = new StringBuilder();


        List<String> tokens = List.of(line.split(" "));

        for (String token : tokens) {

            if (token.matches("!!") || token.matches("||")) {
                break;
            }

            if (!latexBuilder.isEmpty()) {
                latexBuilder.append('&');
            }

            if (token.startsWith("!!") || token.endsWith("||")) {
                latexBuilder.append("\\ds");
            } else if (token.startsWith("!") || token.endsWith("|")) {
                latexBuilder.append("\\ms");
            }

            int cols = SymbolLine.getColCount(token);
            String aligned = SymbolLine.isRightAligned(token) ? "R" : "L";
            String processedToken = processToken(token);
            processedToken = processSymbols(processedToken);
            if (cols == 1) {
                latexBuilder.append("\\st{%s}".formatted(processedToken));
            }
            if (cols > 1) {
                latexBuilder.append("\\multiline{%s}{%s}{\\st{%s}}".formatted(cols, aligned, processedToken));
            }
        }

        return latexBuilder.toString();
    }

    private String processSymbols(String token) {
        StringBuilder processedToken = new StringBuilder();

        for (char c : token.toCharArray()) {
            if (c == '>') {
                processedToken.append("\\hfill ");
            } else if (c == '_') {
                processedToken.append(' ');
            } else {
                processedToken.append(c);
            }
        }
        return processedToken.toString();
    }

    static String processToken(String token) {
        String processToken = token.replaceAll("^\\*+", "");
        processToken = processToken.replaceAll("\\*+$", "");
        processToken = processToken.replaceAll("^[!|]+", "");
        processToken = processToken.replace('*', '_');
        return processToken;
    }

    public static boolean matches(String line) {
        return !KeyValueLine.matches(line) && !NoteLine.matches(line) && !SymbolLine.matches(line);
    }
}
