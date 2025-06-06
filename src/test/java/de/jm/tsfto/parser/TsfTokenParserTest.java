package de.jm.tsfto.parser;

import de.jm.tsfto.exception.InvalidNoteRuntimeException;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.model.tsf.TsfToken;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TsfTokenParserTest {

    @Test
    void prefix() {
        assertEquals("", TsfTokenParser.getPrefix(null));
        assertEquals("", TsfTokenParser.getPrefix(""));
        assertEquals("!", TsfTokenParser.getPrefix("!d"));
        assertEquals(".;", TsfTokenParser.getPrefix(".;d"));
    }

    @Test
    void accent() {
        assertEquals(TsfNote.Accent.UNKNOWN, TsfTokenParser.getAccent(null));
        assertEquals(TsfNote.Accent.UNKNOWN, TsfTokenParser.getAccent(""));
        assertEquals(TsfNote.Accent.DOUBLE_BAR, TsfTokenParser.getAccent("!!d"));
        assertEquals(TsfNote.Accent.DOUBLE_BAR, TsfTokenParser.getAccent("||d"));
        assertEquals(TsfNote.Accent.BAR, TsfTokenParser.getAccent("!d"));
        assertEquals(TsfNote.Accent.BAR, TsfTokenParser.getAccent("|d"));
        assertEquals(TsfNote.Accent.ACCENTED, TsfTokenParser.getAccent(";d"));
        assertEquals(TsfNote.Accent.NONE, TsfTokenParser.getAccent(":d"));
        assertEquals(TsfNote.Accent.NONE, TsfTokenParser.getAccent(".d"));
    }

    @Test
    void length() {
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getLength(null));
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getLength(""));

        assertEquals(TsfNote.Length.FULL, TsfTokenParser.getLength("!!d"));
        assertEquals(TsfNote.Length.FULL, TsfTokenParser.getLength("||d"));
        assertEquals(TsfNote.Length.FULL, TsfTokenParser.getLength("!d"));
        assertEquals(TsfNote.Length.FULL, TsfTokenParser.getLength("|d"));
        assertEquals(TsfNote.Length.FULL, TsfTokenParser.getLength(";d"));
        assertEquals(TsfNote.Length.FULL, TsfTokenParser.getLength(":d"));
        assertEquals(TsfNote.Length.HALF_QUARTER, TsfTokenParser.getLength(".,d"));
        assertEquals(TsfNote.Length.TWO_THIRDS, TsfTokenParser.getLength("//d"));
        assertEquals(TsfNote.Length.HALF, TsfTokenParser.getLength(".d"));
        assertEquals(TsfNote.Length.THIRD, TsfTokenParser.getLength("/d"));
        assertEquals(TsfNote.Length.QUARTER, TsfTokenParser.getLength(",d"));
        assertEquals(TsfNote.Length.EIGHTS, TsfTokenParser.getLength("d"));
    }

    @Test
    void note() {
        assertEquals("", TsfTokenParser.getNote(null));
        assertEquals("", TsfTokenParser.getNote(""));

        assertEquals("da", TsfTokenParser.getNote("da"));
        assertEquals("d", TsfTokenParser.getNote(":d"));
        assertEquals("di", TsfTokenParser.getNote(";di,"));
        assertEquals("ra", TsfTokenParser.getNote(":ra'"));
        assertEquals("t", TsfTokenParser.getNote("!t''_"));
        assertEquals("ba", TsfTokenParser.getNote("!ba="));

        assertEquals("t", TsfTokenParser.getNote(":l-t,"));

        assertThrows(InvalidNoteRuntimeException.class, () -> TsfTokenParser.getNote(".bi"));
    }

    @Test
    void keyChangeNote() {
        assertEquals("l", TsfTokenParser.getKeyChangeNote(":l-t,"));
        assertEquals("", TsfTokenParser.getKeyChangeNote(null));
        assertEquals("", TsfTokenParser.getKeyChangeNote(""));

        assertEquals("", TsfTokenParser.getKeyChangeNote(":l,"));
    }

    @Test
    void octave() {

    }

    @Test
    void keyChangeOctave() {

    }

}
