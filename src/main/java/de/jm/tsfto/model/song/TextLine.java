package de.jm.tsfto.model.song;

import java.util.List;

public class TextLine extends SongLine {

    public TextLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        StringBuilder latexBuilder = new StringBuilder();

        List<String> tokens = List.of(line.split(" +"));

        for (String token : tokens) {


            int cols = SymbolLine.getColCount(token);
            String aligned = SymbolLine.isRightAligned(token) ? "R" : "L";
            String processedToken = processToken(token);
            processedToken = processSymbols(processedToken);
            if (processedToken.isEmpty()) {
                latexBuilder.append("&".repeat(cols));
            } else {
                String songtext = "";

                if (token.startsWith("!!") || token.endsWith("||")) {
                    songtext = "\\ds";
                } else if (token.startsWith("!") || token.endsWith("|")) {
                    songtext = "\\ms";
                } else {
                    processedToken = "\\ " +  processedToken;
                }

                songtext += "\\st{%s}".formatted(processedToken);

                if (cols == 1) {
                    latexBuilder.append(songtext).append("&");
                }
                if (cols > 1) {
                    latexBuilder.append("\\multicolumn{%s}{%s}{%s}".formatted(cols, aligned, songtext)).append("&");
                }
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
