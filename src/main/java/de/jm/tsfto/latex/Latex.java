package de.jm.tsfto.latex;

import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.TsfTokenParser;

import java.util.HashMap;
import java.util.Map;

public class Latex {

    private static final Map<String, String> prefixToLatex = new HashMap<>();

    static {
        prefixToLatex.put("!", "m");
        prefixToLatex.put("|", "m");
        prefixToLatex.put("!!", "d");
        prefixToLatex.put("||", "d");
        prefixToLatex.put(";", "a");
        prefixToLatex.put(":", "n");
        prefixToLatex.put(".,", "hq");
       prefixToLatex.put(",,", "hhq");
        prefixToLatex.put("//", "tt");
        prefixToLatex.put(".", "h");
        prefixToLatex.put(",", "q");
        prefixToLatex.put("/", "t");
        prefixToLatex.put("", "e");
    }

    public static final Map<String, String> prefixToPlainLatex = new HashMap<>();

    static {
        prefixToPlainLatex.put("!", "\\ms");
        prefixToPlainLatex.put("|", "\\ms");
        prefixToPlainLatex.put("!!", "\\ds");
        prefixToPlainLatex.put("||", "\\ds");
        prefixToPlainLatex.put(";", "\\as");
        prefixToPlainLatex.put(":", "\\ns");
        prefixToPlainLatex.put(".,", "\\hfill\\hs&\\qs");
        prefixToPlainLatex.put(",,", "\\hs\\qs");
        prefixToPlainLatex.put("//", "\\tts");
        prefixToPlainLatex.put(".", "\\hs");
        prefixToPlainLatex.put(",", "\\qs");
        prefixToPlainLatex.put("/", "\\ts");
        prefixToPlainLatex.put("", "\\es");
    }

    public static String tsfNoteToLatex(TsfNote note) {
        if (note.isEndOfPart()) {
            return "";
        }

        StringBuilder latexBuilder = new StringBuilder("\\");
        int colCount = note.getColCount();

        if (colCount == 2) {
            latexBuilder.append("d");
        }

        latexBuilder.append(prefixToLatex.get(note.getPrefix()));

        if (note.isContinue()) {
            latexBuilder.append("c");
            latexBuilder.append(note.isCaesura() ? "\\tsfCaesura" : "");
            latexBuilder.append(note.isBreath() ? "\\tsfBreath" : "");
            latexBuilder.append(getUnderLatex(note.getUnderLength(), note.isUnderline()));
        } else if (note.isBreak()) {
            latexBuilder.append("b");
            latexBuilder.append(getUnderLatex(note.getUnderLength(), note.isUnderline()));
        } else if (note.isKeyChange()) {
            latexBuilder.append("kc");
            TsfNote secondNote = TsfTokenParser.getPlainNote(note.getSecondNote());
            latexBuilder.append("{\\p").append(getLatexNoteString(note.getOctave(), note.getNote(), note.getUnderLength(), note.isUnderline(), note)).append('}');
            latexBuilder.append("{\\p").append(getLatexNoteString(secondNote.getOctave(), secondNote.getNote(), secondNote.getUnderLength(), secondNote.isUnderline(), secondNote)).append('}');
        } else if (note.isNote()) {
            latexBuilder.append(getLatexNoteString(note.getOctave(), note.getNote(), note.getUnderLength(), note.isUnderline(), note));
        }

        if (colCount > 2) {
            return "\\multicolumn{" + colCount + "}{L}{" + latexBuilder + "}";
        } else {
            return latexBuilder.toString();
        }
    }

    static String getLatexNoteString(int octave, String note, String underLength, boolean underline, TsfNote tsfNote) {

        String noteString;

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

        String under = getUnderLatex(underLength, underline);

        if (tsfNote.isPortato()) {
            note = "\\tsfPort{" + note + "}";
        } else {
            if (tsfNote.isTenuto()) {
                note = "\\tsfTen{" + note + "}";
            }
            if (tsfNote.isStaccato()) {
                note = "\\tsfStac{" + note + "}";
            }
        }
        if (tsfNote.isAccented()) {
            note = "\\tsfAcc{" + note + "}";
        }
        if (tsfNote.isMarcato()) {
            note = "\\tsfMark{" + note + "}";
        }

        if (tsfNote.isStack()) {
            note = "\\lstack{" + note + "}{" + tsfNote.getSecondNote() + "}";
        }

        if (tsfNote.isText()) {
            note = tsfNote.getPostfix();
        }

        String caesura = tsfNote.isCaesura() ? "\\tsfCaesura" : "";
        String breath = tsfNote.isBreath() ? "\\tsfBreath" : "";

        return noteString + under + "{" + note + "}" + breath + caesura;
    }

    private static String getUnderLatex(String underLength, boolean underline) {
        if (!underLength.isEmpty()) {
            if (underline) {
                return "u[" + underLength + "]";
            } else {
                return "p[" + underLength + "]";
            }
        }
        return "";
    }

    public static String twoNotesMultiColumnToLatex(TsfNote note, TsfNote otherNote) {
        return "\\multicolumn{2}{L}{" + tsfNoteToLatex(note) + "\\hfill" + tsfNoteToLatex(otherNote) + "}";
    }

    public static String getPlainNoteLatex(TsfNote tsfNote) {
        StringBuilder latexBuilder = new StringBuilder();
        int octave = tsfNote.getOctave();
        String note = tsfNote.getNote();
        if (octave < 0) {
            latexBuilder.append("\\lo[%s]{%s}".formatted(-octave, note));
        } else if (octave > 0) {
            latexBuilder.append("\\hi[%s]{%s}".formatted(octave, note));
        } else {
            latexBuilder.append(note);
        }
        return latexBuilder.toString();
    }
}
