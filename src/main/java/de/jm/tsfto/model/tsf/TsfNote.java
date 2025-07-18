package de.jm.tsfto.model.tsf;

import de.jm.tsfto.exception.InvalidNoteRuntimeException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TsfNote {
    public static List<String> validNotes = List.of(
            "da", "d", "di", "ra", "r", "ri", "ma", "m", "mi", "fa", "f", "fi", "sa", "s", "si", "la", "l", "li", "ta", "t", "ti", "ba", "-", "\""
    );

    public enum Length {EIGHTS, SIXTH, QUARTER, THIRD, HALF, TWO_THIRDS, HALF_QUARTER, FULL, UNKNOWN}

    public enum Accent {BAR, DOUBLE_BAR, ACCENTED, NONE, UNKNOWN}

    public enum Type {NOTE, CONTINUE, BREAK, END_OF_PART}

    private final int octave;
    private final String note;
    private final Length length;
    private final Accent accent;
    private final String prefix;
    private final String postfix;

    public TsfNote(int octave, String note, Length length, Accent accent, String prefix, String postfix) {
        this.octave = octave;
        this.note = note;
        this.length = length;
        this.accent = accent;
        this.prefix = prefix;
        this.postfix = postfix;
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

    public boolean isKeyChange() {
        return postfix.contains("-");
    }

    public boolean isContinue() {
        return Type.CONTINUE == getType();
    }

    public boolean isTenuto() {
        return postfix.contains("~");
    }

    public boolean isStaccato() {
        return postfix.matches("(^|\\D)\\.(\\D|$)");
    }

    public boolean isPortato() {
        return isStaccato() && isTenuto();
    }

    public boolean isAccented() {
        return postfix.contains(">");
    }

    public boolean isMarcato() {
        return postfix.contains("^");
    }

    public boolean isText() {
        return note.startsWith("\"");
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

    public boolean isStack() {
        return postfix.contains("%");
    }

    public boolean isCaesura() {
        return postfix.contains("//");
    }

    public boolean isBreath() {
        return !isCaesura() && postfix.contains("/");
    }

    public String getSecondNote() {
        Pattern p = Pattern.compile("[-%]([a-z]+[,']*)");
        if (isStack() || isKeyChange()) {
            Matcher matcher = p.matcher(postfix);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    public boolean isDoubleCol() {
        return postfix.contains("*");
    }

    public int getColCount() {
        int count = 1;
        for (char c : postfix.toCharArray()) {
            if (c == '*') {
                count++;
            }
        }
        return count;
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

    @Override
    public String toString() {
        return prefix + note + postfix;
    }
}
