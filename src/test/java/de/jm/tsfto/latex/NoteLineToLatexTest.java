package de.jm.tsfto.latex;

import de.jm.tsfto.model.song.NoteLine;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.TsfTokenParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.jm.tsfto.latex.Latex.tsfNoteToLatex;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoteLineToLatexTest {
    @Test
    void noteLineToColWidth() {
        assertEquals("HQQ", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(":d .d ,d")));
        assertEquals("HQQ", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(":d .- ,d")));
        assertEquals("HQQ", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(":d .  ,s")));
        assertEquals("F", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(":d")));
        assertEquals("H", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(".d")));
        assertEquals("HH", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(":d .d")));
        assertEquals("HQQ", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(":d .,d")));
        assertEquals("HQQ", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(":d ,,d")));
        assertEquals("TTTHH", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(";d /d /d :r .m")));
        assertEquals("TTTHH", NoteLineToLatex.tsfNotesToLatex(lineToTsfNotes(";d  //d :r .m")));
    }

    //(int octave, String note, TsfNote.Length length, TsfNote.Accent accent, String prefix, String postfix, int keyChangeOctave, String keyChangeNote) {
    @Test
    void tsfNotesToLatex() {
        assertEquals("\\dn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.DOUBLE_BAR, "!!", "")));
        assertEquals("\\mn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.BAR, "!", "")));
        assertEquals("\\mh{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.FULL, TsfNote.Accent.BAR, "!", "")));
        assertEquals("\\an{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.ACCENTED, ";", "")));
        assertEquals("\\al[2]{d}", tsfNoteToLatex(new TsfNote(-2, "d", TsfNote.Length.FULL, TsfNote.Accent.ACCENTED, ";", "")));
        assertEquals("\\nn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.NONE, ":", "")));

        assertEquals("\\hn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ".", "")));
        assertEquals("\\hqn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF_QUARTER, TsfNote.Accent.UNKNOWN, ".,", "")));
        assertEquals("\\tn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.THIRD, TsfNote.Accent.UNKNOWN, "/", "")));
        assertEquals("\\ttn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.TWO_THIRDS, TsfNote.Accent.UNKNOWN, "//", "")));
        assertEquals("\\en{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.EIGHTS, TsfNote.Accent.UNKNOWN, "", "")));


        assertEquals("\\nc", tsfNoteToLatex(new TsfNote(0, "-", TsfNote.Length.FULL, TsfNote.Accent.NONE, ":", "")));
        assertEquals("\\mc", tsfNoteToLatex(new TsfNote(0, "-", TsfNote.Length.FULL, TsfNote.Accent.BAR, "|", "")));

        assertEquals("\\nb", tsfNoteToLatex(new TsfNote(0, "", TsfNote.Length.FULL, TsfNote.Accent.NONE, ":", "")));
        assertEquals("\\mb", tsfNoteToLatex(new TsfNote(0, "", TsfNote.Length.FULL, TsfNote.Accent.BAR, "|", "")));

        assertEquals("", tsfNoteToLatex(new TsfNote(0, "", TsfNote.Length.UNKNOWN, TsfNote.Accent.DOUBLE_BAR, "|", "")));

        assertEquals("\\mkc{\\pn{d}}{\\pn{r}}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.BAR, "|", "-r")));
        assertEquals("\\nkc{\\pn{d}}{\\pn{r}}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.NONE, ":", "-r")));
        assertEquals("\\akc{\\pn{d}}{\\pn{r}}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.ACCENTED, ";", "-r")));
        assertEquals("\\dkc{\\pn{d}}{\\pn{r}}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.DOUBLE_BAR, "!!", "-r")));

        // underline/underpoint d_5
        assertEquals("\\hnu[1]{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ".", "_")));
        assertEquals("\\hnu[2]{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ".", "_2")));
        assertEquals("\\mhp[1]{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.HALF, TsfNote.Accent.BAR, "!", "=")));
        assertEquals("\\ahp[2.5]{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.HALF, TsfNote.Accent.ACCENTED, ";", "=2.5")));

        // one note in two columns (d*)
        assertEquals("\\dhh{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.HALF, TsfNote.Accent.NONE, ".", "*")));
        assertEquals("\\dhc", tsfNoteToLatex(new TsfNote(0, "-", TsfNote.Length.HALF, TsfNote.Accent.NONE, ".", "*")));

        // two notes in one column (d+ r)
        assertEquals("\\multicolumn{2}{L}{\\mn{d}\\hfill\\hh{r}}", Latex.twoNotesMultiColumnToLatex(
                new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.BAR, "!", ""),
                new TsfNote(1, "r", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ".", "")
        ));

    }

    private List<TsfNote> lineToTsfNotes(String line) {
        return TsfTokenParser.parse(NoteLine.getTokens(line));
    }
}
