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
        assertEquals("\\dn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.DOUBLE_BAR, "!!", "", 0, null)));
        assertEquals("\\mn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.BAR, "!", "", 0, null)));
        assertEquals("\\mh{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.FULL, TsfNote.Accent.BAR, "!", "", 0, null)));
        assertEquals("\\an{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.ACCENTED, ";", "", 0, null)));
        assertEquals("\\al[2]{d}", tsfNoteToLatex(new TsfNote(-2, "d", TsfNote.Length.FULL, TsfNote.Accent.ACCENTED, ";", "", 0, null)));
        assertEquals("\\nn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.NONE, ":", "", 0, null)));

        assertEquals("\\hn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ".", "", 0, null)));
        assertEquals("\\hqn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF_QUARTER, TsfNote.Accent.UNKNOWN, ".", "", 0, null)));
        assertEquals("\\tn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.THIRD, TsfNote.Accent.UNKNOWN, ".", "", 0, null)));
        assertEquals("\\ttn{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.TWO_THIRDS, TsfNote.Accent.UNKNOWN, "//", "", 0, null)));
        assertEquals("\\en{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.EIGHTS, TsfNote.Accent.UNKNOWN, ".", "", 0, null)));


        assertEquals("\\nc", tsfNoteToLatex(new TsfNote(0, "-", TsfNote.Length.FULL, TsfNote.Accent.NONE, ":", "", 0, null)));
        assertEquals("\\mc", tsfNoteToLatex(new TsfNote(0, "-", TsfNote.Length.FULL, TsfNote.Accent.BAR, "|", "", 0, null)));

        assertEquals("\\nb", tsfNoteToLatex(new TsfNote(0, "", TsfNote.Length.FULL, TsfNote.Accent.NONE, ":", "", 0, null)));
        assertEquals("\\mb", tsfNoteToLatex(new TsfNote(0, "", TsfNote.Length.FULL, TsfNote.Accent.BAR, "|", "", 0, null)));

        assertEquals("", tsfNoteToLatex(new TsfNote(0, "", TsfNote.Length.UNKNOWN, TsfNote.Accent.DOUBLE_BAR, "|", "", 0, null)));

        assertEquals("\\mkc{\\nn{d}}{\\nn{r}}", tsfNoteToLatex(new TsfNote(0, "r", TsfNote.Length.FULL, TsfNote.Accent.BAR, "|", "", 0, "d")));

        // underline/underpoint d_5
        assertEquals("\\hnu[1]{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ":", "_", 0, null)));
        assertEquals("\\hnu[2]{d}", tsfNoteToLatex(new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ":", "_2", 0, null)));
        assertEquals("\\mhp[1]{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.HALF, TsfNote.Accent.BAR, ":", "=", 0, null)));
        assertEquals("\\ahp[2.5]{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.HALF, TsfNote.Accent.ACCENTED, ":", "=2.5", 0, null)));

        // one note in two columns (d*)
        assertEquals("\\dnh{d}", tsfNoteToLatex(new TsfNote(1, "d", TsfNote.Length.HALF, TsfNote.Accent.NONE, ".", "*", 0, null)));

        // two notes in one column (d+ r)
        assertEquals("\\mn{d}\\hfill\\hh{r}", Latex.twoNotesOneColumnToLatex(
                new TsfNote(0, "d", TsfNote.Length.HALF, TsfNote.Accent.BAR, "!", "", 0, null),
                new TsfNote(1, "r", TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, ".", "", 0, null)
        ));

    }

    private List<TsfNote> lineToTsfNotes(String line) {
        return TsfTokenParser.parse(NoteLine.getTokens(line));
    }
}
