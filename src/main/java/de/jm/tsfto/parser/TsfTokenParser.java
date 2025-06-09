package de.jm.tsfto.parser;

import de.jm.tsfto.exception.InvalidNoteRuntimeException;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.model.tsf.TsfToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.jm.tsfto.model.tsf.TsfNote.Accent;
import static de.jm.tsfto.model.tsf.TsfNote.validNotes;
import static de.jm.tsfto.parser.TsfParser.TOKEN_NOTES;

public class TsfTokenParser {

    static String validNoteFirstChars = "drmfsltb- ";
    static String validNoteSecondChars = "ai";

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
            TsfNote.Length.HALF, TsfNote.Length.HALF,
            TsfNote.Length.QUARTER, TsfNote.Length.QUARTER,
            TsfNote.Length.HALF_QUARTER, TsfNote.Length.QUARTER,
            TsfNote.Length.THIRD, TsfNote.Length.THIRD,
            TsfNote.Length.TWO_THIRDS, TsfNote.Length.THIRD,
            TsfNote.Length.EIGHTS, TsfNote.Length.EIGHTS
    );

    private final List<TsfToken> tokens;
    private final List<TsfNote> tsfNotes = new ArrayList<>();
    private int pos;

    TsfTokenParser(List<TsfToken> tokens) {
        this.tokens = tokens;
    }

    public static List<TsfNote> parse(List<TsfToken> tokens) {
        return new TsfTokenParser(tokens).parse();
    }

    private List<TsfNote> parse() {
        for (pos = 0; pos < tokens.size(); pos++) {
            tsfNotes.add(new TsfNote(getOctave(), getNote(), getLength(), getAccent(), getPostfix(), getKeyChangeOctave(), getKeyChangeNote()));
        }
        return tsfNotes;
    }

    public TsfToken current() {
        return tokens.get(pos);
    }

    public TsfToken next() {
        if (pos + 1 < tokens.size()) {
            return tokens.get(pos + 1);
        } else {
            return null;
        }
    }

    int getOctave() {
        return getOctave(current().toString());
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
        return getNote(current().toString());
    }

    static String getNote(String tsfToken) {
        if (tsfToken == null || tsfToken.isEmpty()) {
            throw new InvalidNoteRuntimeException(tsfToken);
        }

        String note = getFirstNote(tsfToken);
        String afterNote = tsfToken.substring(note.length() + getPrefix(tsfToken).length());

        if (!afterNote.isEmpty() && '-' == afterNote.charAt(0)) {
            // key change
            note = getNote(afterNote.substring(1));
        }

        return note;
    }

    TsfNote.Length getLength() {
        TsfNote.Length length = getLength(current().toString());

        if (length == TsfNote.Length.UNKNOWN) {
            TsfToken next = next();
            if (next != null) {
                length = getLength(next.toString());

                if (length == TsfNote.Length.UNKNOWN) {
                    length = TsfNote.Length.FULL;
                } else {
                    length = remainingLengths.get(length);
                }
            }
        }

        return length;
    }

    static TsfNote.Length getLength(String token) {
        if (token == null || token.isEmpty()) {
            throw new InvalidNoteRuntimeException(token);
        }

        return prefixToLength.getOrDefault(getPrefix(token), TsfNote.Length.UNKNOWN);
    }

    TsfNote.Accent getAccent() {
        return getAccent(current().toString());
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
        return getPostfix(current().toString());
    }

    static String getPostfix(String tsfToken) {
        if (tsfToken == null || tsfToken.isEmpty()) {
            return "";
        }

        String prefix = getPrefix(tsfToken);
        String note = getNote(tsfToken);
        String keyChangeNote = getKeyChangeNote(tsfToken);
        int octave = getOctave(tsfToken);
        int keyChangeOctave = getKeyChangeOctave(tsfToken);

        return tsfToken.substring(prefix.length() + note.length() + keyChangeNote.length() + Math.abs(octave) + Math.abs(keyChangeOctave));
    }

    int getKeyChangeOctave() {
        return getKeyChangeOctave(current().toString());
    }

    static int getKeyChangeOctave(String tsfToken) {
        if (tsfToken == null || !tsfToken.contains("-")) {
            return 0;
        }

        String[] notes = tsfToken.split("-");

        return getOctave(notes[0]);
    }

    String getKeyChangeNote() {
        return getKeyChangeNote(current().toString());
    }

    static String getKeyChangeNote(String tsfToken) {
        if (tsfToken == null || !tsfToken.contains("-")) {
            return "";
        }

        String[] notes = tsfToken.split("-");

        return getNote(notes[0]);

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
}
