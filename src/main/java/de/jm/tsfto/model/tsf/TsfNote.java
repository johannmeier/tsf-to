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
    private final String prefix;
    private final String postfix;
    private final int keyChangeOctave;
    private final String keyChangeNote;

    public TsfNote(int octave, String note, Length length, Accent accent, String prefix, String postfix, int keyChangeOctave, String keyChangeNote) {
        this.octave = octave;
        this.note = note;
        this.length = length;
        this.accent = accent;
        this.prefix = prefix;
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

    public String getPrefix() {
        return prefix;
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


    public boolean isContinue() {
        return Type.CONTINUE == getType();
    }

    public boolean isBreak() {
        return Type.BREAK == getType();
    }

    public boolean isEndOfPart() {
        return Type.END_OF_PART == getType();
    }

    public boolean isNote() {
        return Type.NOTE == getType();
    }

    public boolean isKeyChange() {
        return getKeyChangeNote() != null && !getKeyChangeNote().isEmpty();
    }

    public boolean isDoubleCol() {
        return postfix.contains("*");
    }

    public boolean isTwoNotesOneColumn() {
        return postfix.contains("+");
    }


    public boolean isUnderline() {
        return postfix.contains("_");
    }

    public boolean isUnderpoint() {
        return postfix.contains("=");
    }

    public String getUnderLength() {
        int posOfLength = -1;
        if (isUnderline()) {
            posOfLength = postfix.indexOf("_");
        } else if (isUnderpoint()) {
            posOfLength = postfix.indexOf("=");
        }

        if  (posOfLength == -1) {
            return "";
        }

        StringBuilder lengthBuilder = new StringBuilder();
        for (char c : postfix.substring(posOfLength+1).toCharArray()) {
            if ("0123456789.".contains(String.valueOf(c))) {
                lengthBuilder.append(c);
            } else {
                break;
            }
        }

        if (lengthBuilder.isEmpty()) {
            lengthBuilder.append(1);
        }

        return lengthBuilder.toString();
    }
}
