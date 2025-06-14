package de.jm.tsfto.latex;

import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.TsfLineParser;
import de.jm.tsfto.parser.TsfTokenParser;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private List<TsfNote> lineToTsfNotes(String line) {
        TsfLineParser tsfLineParser = new TsfLineParser(line);
        return TsfTokenParser.parse(tsfLineParser.parse());
    }
}
