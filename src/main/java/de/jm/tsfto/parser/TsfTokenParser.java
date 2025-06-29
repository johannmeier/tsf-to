package de.jm.tsfto.parser;

import de.jm.tsfto.exception.InvalidNoteRuntimeException;
import de.jm.tsfto.model.tsf.TsfNote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.jm.tsfto.model.tsf.TsfNote.Accent;
import static de.jm.tsfto.model.tsf.TsfNote.validNotes;

public class TsfTokenParser {

    public static final String TOKEN_NOTES = "drmfsltb-";
    static final String validNoteFirstChars = TOKEN_NOTES + " ";
    static final String validNoteSecondChars = "ai";

    static Map<String, TsfNote.Length> prefixToLength = Map.of(
            ".", TsfNote.Length.HALF,
            ".,", TsfNote.Length.HALF_QUARTER,
            ",,", TsfNote.Length.HALF_QUARTER,
            "//", TsfNote.Length.TWO_THIRDS,
            ",", TsfNote.Length.QUARTER,
            "/", TsfNote.Length.THIRD,
            "", TsfNote.Length.EIGHTS
    );

    static Map<String, TsfNote.Accent> prefixToAccent = Map.of(
            "!!", Accent.DOUBLE_BAR,
            "||", Accent.DOUBLE_BAR,
            "!", Accent.BAR,
            "|", Accent.BAR,
            ";", Accent.ACCENTED,
            ":", Accent.NONE
    );

    static Map<TsfNote.Length, TsfNote.Length> remainingLengths = Map.of(
            TsfNote.Length.FULL, TsfNote.Length.FULL,
            TsfNote.Length.HALF, TsfNote.Length.HALF,
            TsfNote.Length.QUARTER, TsfNote.Length.QUARTER,
            TsfNote.Length.HALF_QUARTER, TsfNote.Length.QUARTER,
            TsfNote.Length.THIRD, TsfNote.Length.THIRD,
            TsfNote.Length.TWO_THIRDS, TsfNote.Length.THIRD,
            TsfNote.Length.EIGHTS, TsfNote.Length.EIGHTS,
            TsfNote.Length.UNKNOWN, TsfNote.Length.UNKNOWN
    );

    private final List<String> tokens;
    private final List<TsfNote> tsfNotes = new ArrayList<>();
    private int pos;

    TsfTokenParser(List<String> tokens) {
        this.tokens = tokens;
    }

    public static List<TsfNote> parse(List<String> tokens) {
        return new TsfTokenParser(tokens).parse();
    }

    private List<TsfNote> parse() {
        for (pos = 0; pos < tokens.size(); pos++) {
            TsfNote.Length length = getLength();
            if (length == TsfNote.Length.UNKNOWN && !isEndToken(current()) && next() == null) {
                length = TsfNote.Length.FULL;
            }
            tsfNotes.add(new TsfNote(getOctave(), getNote(), length, getAccent(), getPrefix(), getPostfix()));
        }
        return tsfNotes;
    }

    public String current() {
        return tokens.get(pos);
    }

    public String next() {
        if (pos + 1 < tokens.size()) {
            return tokens.get(pos + 1);
        } else {
            return null;
        }
    }

    int getOctave() {
        return getOctave(current());
    }

    static int getOctave(String tsfToken) {
        int pos = getPrefix(tsfToken).length() + getNote(tsfToken).length();
        String token = tsfToken.substring(pos);
        int octave = 0;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == ',') {
                octave--;
                continue;
            }
            if (c == '\'') {
                octave++;
                continue;
            }
            break;
        }
        return octave;
    }

    String getNote() {
        return getNote(current());
    }

    static String getNote(String tsfToken) {
        if (tsfToken == null || tsfToken.isEmpty()) {
            throw new InvalidNoteRuntimeException(tsfToken);
        }

        return getFirstNote(tsfToken);
    }

    TsfNote.Length getLength() {
        TsfNote.Length length = remainingLengths.get(getTsfSignLength(current()));
        String next = next();

        if (next != null) {
            String prefix = getPrefix(next);
            if (prefixToLength.containsKey(prefix)) {
                length = min(length, getTsfSignLength(next));
            }

            if (length == TsfNote.Length.UNKNOWN) {
                length = TsfNote.Length.FULL;
            }
        }

        return length;
    }

    static TsfNote.Length min(TsfNote.Length length1, TsfNote.Length length2) {
        if (length1.compareTo(length2) < 0) {
            return length1;
        } else {
            return length2;
        }
    }

    static TsfNote.Length getTsfSignLength(String token) {
        if (token == null || token.isEmpty()) {
            throw new InvalidNoteRuntimeException(token);
        }

        return prefixToLength.getOrDefault(getPrefix(token), TsfNote.Length.UNKNOWN);
    }

    TsfNote.Accent getAccent() {
        return getAccent(current());
    }

    static TsfNote.Accent getAccent(String tsfToken) {
        if (tsfToken == null || tsfToken.isEmpty()) {
            return Accent.UNKNOWN;
        }

        String prefix = getPrefix(tsfToken);
        TsfNote.Accent accent = prefixToAccent.get(prefix);
        if (accent == null) {
            accent = Accent.UNKNOWN;
        }
        return accent;
    }

    String getPrefix() {
        return getPrefix(current());
    }

    static String getPrefix(String token) {
        if (token == null) {
            return "";
        }

        StringBuilder prefix = new StringBuilder();
        for (char c : token.toCharArray()) {
            if (TOKEN_NOTES.contains(Character.toString(c)) || c == '-' || c == ' ') {
                break;
            }
            prefix.append(c);
        }

        return prefix.toString();
    }

    String getPostfix() {
        return getPostfix(current());
    }

    static String getPostfix(String tsfToken) {
        if (tsfToken == null || tsfToken.isEmpty()) {
            return "";
        }

        String prefix = getPrefix(tsfToken);
        String note = getNote(tsfToken);
        int octave = getOctave(tsfToken);

        return tsfToken.substring(prefix.length() + note.length() + Math.abs(octave));
    }

    static int getKeyChangeOctave(String tsfToken) {
        if (tsfToken == null || !tsfToken.contains("-")) {
            return 0;
        }

        String[] notes = tsfToken.split("-");

        return getOctave(notes[0]);
    }

    private static String getFirstNote(String tsfToken) {
        if (tsfToken == null || tsfToken.isEmpty()) {
            throw new InvalidNoteRuntimeException(tsfToken);
        }

        String prefix = getPrefix(tsfToken);
        String token = tsfToken.substring(prefix.length());
        int lengthOfToken = token.length();
        if (lengthOfToken == 0) {
            return "";
        }

        StringBuilder note = new StringBuilder();

        if (validNoteFirstChars.contains(Character.toString(token.charAt(0)))) {
            note.append(token.charAt(0));
        }

        if (lengthOfToken > 1 && validNoteSecondChars.contains(Character.toString(token.charAt(1)))) {
            note.append(token.charAt(1));
        }

        if (validNotes.contains(note.toString())) {
            return note.toString();
        } else {
            throw new InvalidNoteRuntimeException(note.toString());
        }
    }

    public static boolean isEndToken(String token) {
        return "!!".equals(token) || "||".equals(token);
    }

    public static TsfNote getPlainNote(String token) {
        return new TsfNote(getOctave(token), getNote(token), TsfNote.Length.UNKNOWN, TsfNote.Accent.UNKNOWN,
                           getPrefix(token), getPostfix(token));
    }
}
