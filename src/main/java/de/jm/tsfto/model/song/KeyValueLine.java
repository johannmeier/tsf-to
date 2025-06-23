package de.jm.tsfto.model.song;

import de.jm.tsfto.exception.InvalidKeyRuntimeException;

import java.util.HashMap;
import java.util.Map;

public class KeyValueLine extends SongLine {

    private static final Map<String, String> keyToLatex = new HashMap<>();
    static {
        keyToLatex.put("C", "\\renewcommand\\SongComposer{%s}"); // composer
        keyToLatex.put("L", "\\renewcommand\\SongLanguage{%s}"); // language
        keyToLatex.put("T", "\\renewcommand\\SongTitle{%s}"); // title
        keyToLatex.put("X", "\\renewcommand\\SongNumber{%s}"); // song number
        keyToLatex.put("Z", "\\renewcommand\\SongTranscription{%s}"); // transcription
        keyToLatex.put("c", "%% %s"); // comment
        keyToLatex.put("K", "\\renewcommand\\SongPitch{%s}"); // key
        keyToLatex.put("latex", "%s");
        keyToLatex.put("sql", "\\setqlength{%s}");
        keyToLatex.put("left", "left=%s");
        keyToLatex.put("right", "right=%s");
        keyToLatex.put("top", "top=%s");
        keyToLatex.put("bottom", "bottom=%s");
        keyToLatex.put("bpm", "bpm=%s");
        keyToLatex.put("pulse", "bpm=%s");
        keyToLatex.put("fontsize", "\\%s");
        keyToLatex.put("newpage", "\\newpage");
        keyToLatex.put("landscape", "TODO");
        keyToLatex.put("normal", "\\renewcommand\\stfs{\\normalsize}");
        keyToLatex.put("small", "\\renewcommand\\stfs{\\footnotesize}");
        keyToLatex.put("smaller", "\\renewcommand\\stfs{\\scriptsize}");
        keyToLatex.put("tiny", "\\renewcommand\\stfs{\\small}");

    }

    public KeyValueLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        String key = getKey();
        if (keyToLatex.containsKey(key)) {
            return String.format(keyToLatex.get(key), getValue());
        } else {
            throw new InvalidKeyRuntimeException("Unknown key: " + key);
        }
    }

    public String getKey() {
        return getKey(line);
    }

    public static String getKey(String line) {
        int posOfColon = line.indexOf(':');
        if (posOfColon == -1) {
            return "";
        } else {
            return line.substring(0, posOfColon).trim();
        }
    }

    public String getValue() {
        return getValue(line);
    }

    public static boolean isKeyValue(String token) {
        return token.matches("^[a-zA-Z]+:.*") & token.length() > 2;
    }

    public static String getValue(String line) {
        int posOfColon = line.indexOf(':');
        if (posOfColon == -1) {
            return "";
        } else {
            return line.substring(posOfColon + 1).trim();
        }
    }

    public static KeyValueLine of(String line) {
        return new KeyValueLine(line);
    }

    public static boolean matches(String line) {
        boolean match = line.matches("[a-zA-Z]+:.*");
        if (match) {
            return keyToLatex.containsKey(getKey(line));
        }
        return false;
    }
}
