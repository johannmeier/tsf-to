package de.jm.tsfto.latex;

import de.jm.tsfto.model.tsf.TsfNote;

import java.util.HashMap;
import java.util.Map;

public class Latex {

    private static final Map<TsfNote.Accent, String> accentToLatex = new HashMap<>();

    static {
        accentToLatex.put(TsfNote.Accent.BAR, "m");
        accentToLatex.put(TsfNote.Accent.DOUBLE_BAR, "d");
        accentToLatex.put(TsfNote.Accent.ACCENTED, "a");
        accentToLatex.put(TsfNote.Accent.NONE, "n");
        accentToLatex.put(TsfNote.Accent.UNKNOWN, "");
    }

    private static final Map<TsfNote.Length, String> lengthToLatex = new HashMap<>();

    static {
        lengthToLatex.put(TsfNote.Length.HALF_QUARTER, "hq");
        lengthToLatex.put(TsfNote.Length.TWO_THIRDS, "tt");
        lengthToLatex.put(TsfNote.Length.HALF, "h");
        lengthToLatex.put(TsfNote.Length.QUARTER, "q");
        lengthToLatex.put(TsfNote.Length.THIRD, "t");
        lengthToLatex.put(TsfNote.Length.EIGHTS, "e");
    }

    public static String tsfNoteToLatex(TsfNote note) {
        if (note.isEndOfPart()) {
            return "";
        }

        StringBuilder latexBuilder = new StringBuilder("\\");

        if (note.isDoubleCol()) {
            latexBuilder.append("d");
        }

        TsfNote.Accent accent = note.getAccent();
        latexBuilder.append(accentToLatex.get(accent));
        if (accent == TsfNote.Accent.UNKNOWN) {
            latexBuilder.append(lengthToLatex.get(note.getLength()));
        }

        if (note.isContinue()) {
            latexBuilder.append("c");
        } else if (note.isBreak()) {
            latexBuilder.append("b");
        } else if (note.isKeyChange()) {
            latexBuilder.append("kc");
            latexBuilder.append("{\\n").append(getLatexNoteString(note.getKeyChangeOctave(), note.getKeyChangeNote(), note.getUnderLength(), note.isUnderline())).append('}');
            latexBuilder.append("{\\n").append(getLatexNoteString(note.getOctave(), note.getNote(), note.getUnderLength(), note.isUnderline())).append('}');
        } else if (note.isNote()) {
            latexBuilder.append(getLatexNoteString(note.getOctave(), note.getNote(), note.getUnderLength(), note.isUnderline()));
        }

        return latexBuilder.toString();
    }

    static String getLatexNoteString(int octave, String note, String underLength, boolean underline) {

        String noteString;
        String under = "";

        if (octave == 0) {
            noteString = "n";
        } else if (octave == 1) {
            noteString = "h";
        } else if (octave == -1) {
            noteString = "l";
        } else if (octave > 1) {
            noteString = "h[" + octave + "]";
        } else {
            noteString = "l[" + octave * -1 + "]";
        }

        if (!underLength.isEmpty()) {
            if (underline) {
                under = "u[" + underLength + "]";
            } else {
                under = "p[" + underLength + "]";
            }
        }

        return noteString + under + "{" + note + "}";
    }

    public static String twoNotesOneColumnToLatex(TsfNote note, TsfNote otherNote) {
        return tsfNoteToLatex(note) + "\\hfill" + tsfNoteToLatex(otherNote);
    }
}
