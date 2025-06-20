package de.jm.tsfto.model.song;

import java.util.List;

public class TextLine extends SongLine {

    private static final String SONG_TEXT = "\\st{%s}";

    public TextLine(String line) {
        super(KeyValueLine.isKeyValue(line) ? KeyValueLine.getValue(line) : line);
    }

    public static TextLine of(String line) {
        return new TextLine(line);
    }

    @Override
    public String toLatex() {
        StringBuilder latexBuilder = new StringBuilder();

        List<String> tokens = List.of(line.split(" +"));

        for (String token : tokens) {


            int cols = SymbolLine.getColCount(token);
            String aligned = SymbolLine.isRightAligned(token) ? "R" : "L";

            String prefix = "";
            if (token.startsWith("!!") || token.startsWith("||")) {
                prefix = "\\ds";
            } else if (token.startsWith("!") || token.startsWith("|")) {
                prefix = "\\ms";
            }

            String processedToken = processToken(token);
            processedToken = processSymbols(processedToken);

            if (processedToken.isEmpty()) {
                latexBuilder.append(prefix).append("&".repeat(cols));
            } else {


                if (cols == 1) {
                    if (prefix.isEmpty()) {
                        processedToken = "\\ " + processedToken;
                    }
                    String songtext = prefix + SONG_TEXT.formatted(processedToken);
                    latexBuilder.append(songtext).append("&");
                }
                if (cols > 1) {
                    String songtext = prefix + SONG_TEXT.formatted(processedToken);
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
