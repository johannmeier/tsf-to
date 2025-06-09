package de.jm.tsfto.model.tsf;

import de.jm.tsfto.exception.InvalidNoteRuntimeException;

import java.util.List;

public class TsfNote {
    public static List<String> validNotes = List.of(
            "da", "d", "di", "ra", "r", "ri", "ma", "m", "mi", "fa", "f", "fi", "sa", "s", "si", "la", "l", "li", "ta", "t", "ti", "ba", "-"
    );

    public enum Length {FULL, HALF_QUARTER, TWO_THIRDS, HALF, QUARTER, THIRD, EIGHTS, UNKNOWN}

    public enum Accent {BAR, DOUBLE_BAR, ACCENTED, NONE, UNKNOWN}

    public enum Type {NOTE, CONTINUE, BREAK, END_OF_PART}

    private final int octave;
    private final String note;
    private final Length length;
    private final Accent accent;
    private final String postfix;
    private final int keyChangeOctave;
    private final String keyChangeNote;

    public TsfNote(int octave, String note, Length length, Accent accent, String postfix, int keyChangeOctave, String keyChangeNote) {
        this.octave = octave;
        this.note = note;
        this.length = length;
        this.accent = accent;
        this.postfix = postfix;
        this.keyChangeOctave = keyChangeOctave;
        this.keyChangeNote = keyChangeNote;
    }

    public Type getType() {

        if (accent == Accent.DOUBLE_BAR && length == Length.UNKNOWN) {
            return Type.END_OF_PART;
        }

        if (note.isEmpty()) {
            return Type.BREAK;
        }

        if ("-".equals(note)) {
            return Type.CONTINUE;
        }

        if (validNotes.contains(note)) {
            return Type.NOTE;
        } else {
            throw new InvalidNoteRuntimeException(note);
        }
    }

    public int getOctave() {
        return octave;
    }

    public String getNote() {
        return note;
    }

    public Length getLength() {
        return length;
    }

    public Accent getAccent() {
        return accent;
    }

    public String getPostfix() {
        return postfix;
    }

    public int getKeyChangeOctave() {
        return keyChangeOctave;
    }

    public String getKeyChangeNote() {
        return keyChangeNote;
    }
}
