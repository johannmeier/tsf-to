package de.jm.tsfto.model.song;

import de.jm.tsfto.parser.SymbolParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        symbolToLatex.put("_", "\\ ");
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
        keyValueToLatex.put("bpm:", "\\sign{M=%s}");
    }


    public SymbolLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        if (line == null || line.isEmpty()) {
            return "";
        }

        String processedLine = processMultiCols(line);

        StringBuilder latexBuilder = new StringBuilder();
        List<String> tokens = List.of(processedLine.split(" "));

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


    private static final Pattern multiColPattern = Pattern.compile("\\*(\\d+)($|\\s)");

    static String processMultiCols(String line) {
        Matcher matcher = multiColPattern.matcher(line);
        while (matcher.find()) {
            int cols = Integer.parseInt(matcher.group(1));
            line = matcher.replaceFirst("* ".repeat(cols));
        }
        return line;
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
        if (token.startsWith("1.") || token.startsWith("2.")) {
            String[] tokenParts = token.split("_");
            String key = tokenParts[0].substring(0, 2);
            String length = tokenParts[0].substring(2);
            latexBuilder.append(symbolToLatex.get(key));
            if (tokenParts.length > 1) {
                String part2 = tokenParts[1];
                latexBuilder.append("[%s]".formatted(symbolToLatex.getOrDefault(part2, part2)));
            }
            latexBuilder.append("{%s}".formatted(length));
            return latexBuilder.toString();
        }

        List<String> tokenParts = SymbolParser.parse(token);
        for (String part : tokenParts) {
            if (symbolToLatex.containsKey(part)) {
                latexBuilder.append(symbolToLatex.get(part));
            } else {
                if (isKeyValue(part)) {
                    String key = getKey(part) + ":";
                    if (keyValueToLatex.containsKey(key)) {
                        String value = getValue(part);
                        if ("key:".equals(key)) {
                            value = value.replace("b", "\\kfl");
                            value = value.replace("#", "\\ksh");
                        }
                        latexBuilder.append(keyValueToLatex.get(key).formatted(value));
                    }
                } else if (part.charAt(0) == '<' || part.charAt(0) == '>') {
                    String latex = replaceWedges(part);
                    latexBuilder.append(latex.replace(">" , "\\hfill "));
                } else {
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

    static String replaceWedges(String token) {
        Pattern pattern = Pattern.compile("[><]\\d+\\.*\\d*");
        Map<Character, String> wedgesToLatex = Map.of('>', "\\\\decrescWedge{\\\\flc*%s}", '<', "\\\\crescWedge{\\\\flc*%s}");

        while (true) {
            Matcher matcher = pattern.matcher(token);
            if (matcher.find()) {
                String group = matcher.group();
                token = matcher.replaceFirst(wedgesToLatex.get(group.charAt(0)).formatted(group.substring(1)));
            } else {
                break;
            }
        }

        token = token.replaceFirst("<$", wedgesToLatex.get('<').formatted("1"));
        return token.replaceFirst(">$", wedgesToLatex.get('>').formatted("1"));
    }
}
