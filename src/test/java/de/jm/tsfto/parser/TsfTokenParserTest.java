package de.jm.tsfto.parser;

import de.jm.tsfto.exception.InvalidNoteRuntimeException;
import de.jm.tsfto.model.tsf.TsfNote;
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
        assertEquals(TsfNote.Accent.UNKNOWN, TsfTokenParser.getAccent(".d"));

        assertEquals(TsfNote.Accent.DOUBLE_BAR, TsfTokenParser.getAccent("!!d"));
        assertEquals(TsfNote.Accent.DOUBLE_BAR, TsfTokenParser.getAccent("||d"));
        assertEquals(TsfNote.Accent.BAR, TsfTokenParser.getAccent("!d"));
        assertEquals(TsfNote.Accent.BAR, TsfTokenParser.getAccent("|d"));
        assertEquals(TsfNote.Accent.ACCENTED, TsfTokenParser.getAccent(";d"));
        assertEquals(TsfNote.Accent.NONE, TsfTokenParser.getAccent(":d"));
    }

    @Test
    void length() {
        assertThrows(InvalidNoteRuntimeException.class, () -> TsfTokenParser.getTsfSignLength(null));
        assertThrows(InvalidNoteRuntimeException.class, () -> TsfTokenParser.getTsfSignLength(""));

        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getTsfSignLength("!!d"));
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getTsfSignLength("||d"));
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getTsfSignLength("!d"));
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getTsfSignLength("|d"));
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getTsfSignLength(";d"));
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getTsfSignLength(":d"));
        assertEquals(TsfNote.Length.HALF_QUARTER, TsfTokenParser.getTsfSignLength(".,d"));
        assertEquals(TsfNote.Length.HALF_QUARTER, TsfTokenParser.getTsfSignLength(",,d"));
        assertEquals(TsfNote.Length.TWO_THIRDS, TsfTokenParser.getTsfSignLength("//d"));
        assertEquals(TsfNote.Length.HALF, TsfTokenParser.getTsfSignLength(".d"));
        assertEquals(TsfNote.Length.THIRD, TsfTokenParser.getTsfSignLength("/d"));
        assertEquals(TsfNote.Length.QUARTER, TsfTokenParser.getTsfSignLength(",d"));
        assertEquals(TsfNote.Length.EIGHTS, TsfTokenParser.getTsfSignLength("d"));

        // breaks
        assertEquals(TsfNote.Length.UNKNOWN, TsfTokenParser.getTsfSignLength(":"));
        assertEquals(TsfNote.Length.HALF, TsfTokenParser.getTsfSignLength("."));
        assertEquals(TsfNote.Length.QUARTER, TsfTokenParser.getTsfSignLength(","));
        assertEquals(TsfNote.Length.THIRD, TsfTokenParser.getTsfSignLength("/"));
    }

    @Test
    void note() {
        assertThrows(InvalidNoteRuntimeException.class, () -> TsfTokenParser.getNote(null));
        assertThrows(InvalidNoteRuntimeException.class, () -> TsfTokenParser.getNote(""));
        assertThrows(InvalidNoteRuntimeException.class, () -> TsfTokenParser.getNote(".bi"));

        assertEquals("da", TsfTokenParser.getNote("da"));
        assertEquals("d", TsfTokenParser.getNote(":d"));
        assertEquals("di", TsfTokenParser.getNote(";di,"));
        assertEquals("ra", TsfTokenParser.getNote(":ra'"));
        assertEquals("t", TsfTokenParser.getNote("!t''_"));
        assertEquals("ba", TsfTokenParser.getNote("!ba="));

        assertEquals("l", TsfTokenParser.getNote(":l-t,"));

        assertEquals("", TsfTokenParser.getNote("."));
        assertEquals("-", TsfTokenParser.getNote(":-"));
    }

    @Test
    void octave() {
        assertEquals(0, TsfTokenParser.getOctave("d") );
        assertEquals(-1, TsfTokenParser.getOctave("r,") );
        assertEquals(-2, TsfTokenParser.getOctave("m,,") );
        assertEquals(1, TsfTokenParser.getOctave("ma'") );
        assertEquals(2, TsfTokenParser.getOctave("ba''") );
    }

    @Test
    void keyChangeOctave() {
        assertEquals(0, TsfTokenParser.getKeyChangeOctave("d-l") );
        assertEquals(-1, TsfTokenParser.getKeyChangeOctave("r,-di,") );
        assertEquals(-2, TsfTokenParser.getKeyChangeOctave("m,,-s") );
        assertEquals(1, TsfTokenParser.getKeyChangeOctave("ma'-l") );
        assertEquals(2, TsfTokenParser.getKeyChangeOctave("ba''-ta") );
    }

    @Test
    void postFix() {
        assertEquals("/", TsfTokenParser.getPostfix(":-/"));
        assertEquals("+", TsfTokenParser.getPostfix("|s+"));
        assertEquals("", TsfTokenParser.getPostfix(null));
        assertEquals("", TsfTokenParser.getPostfix(""));
        assertEquals("", TsfTokenParser.getPostfix(":d"));
        assertEquals("", TsfTokenParser.getPostfix(":d,"));
        assertEquals("", TsfTokenParser.getPostfix(":d'"));
        assertEquals("hello", TsfTokenParser.getPostfix(":d'hello"));
        assertEquals("_2", TsfTokenParser.getPostfix(":di'_2"));
    }

    @Test
    void parse() {
        List<TsfNote> notes;

        notes = TsfTokenParser.parse(List.of(":d,", ".,m''"));
        assertEquals(2, notes.size());
        assertNote(TsfNote.Length.HALF_QUARTER, TsfNote.Accent.NONE, -1, TsfNote.Type.NOTE, notes.getFirst());
        assertNote(TsfNote.Length.QUARTER, TsfNote.Accent.UNKNOWN, 2, TsfNote.Type.NOTE, notes.get(1));

        notes = TsfTokenParser.parse(List.of("!d", ".m,"));
        assertEquals(2, notes.size());
        assertNote(TsfNote.Length.HALF, TsfNote.Accent.BAR, 0, TsfNote.Type.NOTE, notes.getFirst());
        assertNote(TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, -1, TsfNote.Type.NOTE, notes.get(1));


        notes = TsfTokenParser.parse(List.of("|s+", ",,m"));
        assertEquals(2, notes.size());
        assertNote(TsfNote.Length.HALF_QUARTER, TsfNote.Accent.BAR, 0, TsfNote.Type.NOTE, notes.getFirst());
        assertEquals("+", notes.getFirst().getPostfix());
        assertNote(TsfNote.Length.QUARTER, TsfNote.Accent.UNKNOWN, 0, TsfNote.Type.NOTE, notes.get(1));
        assertEquals(",,", notes.get(1).getPrefix());

        notes = TsfTokenParser.parse(List.of(";-", "."));
        assertEquals(2, notes.size());
        assertNote(TsfNote.Length.HALF, TsfNote.Accent.ACCENTED, 0, TsfNote.Type.CONTINUE, notes.getFirst());
        assertNote(TsfNote.Length.HALF, TsfNote.Accent.UNKNOWN, 0, TsfNote.Type.BREAK, notes.get(1));

        notes = TsfTokenParser.parse(List.of(":d", ".-", ",d'"));
        assertEquals(3, notes.size());
        assertNote(TsfNote.Length.HALF, TsfNote.Accent.NONE, 0, TsfNote.Type.NOTE, notes.getFirst());
        assertNote(TsfNote.Length.QUARTER, TsfNote.Accent.UNKNOWN, 0, TsfNote.Type.CONTINUE, notes.get(1));
        assertNote(TsfNote.Length.QUARTER, TsfNote.Accent.UNKNOWN, 1, TsfNote.Type.NOTE, notes.get(2));

        notes = TsfTokenParser.parse(List.of("||", "!!"));
        assertEquals(2, notes.size());
        assertNote(TsfNote.Length.FULL, TsfNote.Accent.DOUBLE_BAR, 0, TsfNote.Type.BREAK, notes.getFirst());
        assertNote(TsfNote.Length.UNKNOWN, TsfNote.Accent.DOUBLE_BAR, 0, TsfNote.Type.END_OF_PART, notes.get(1));
    }

    private void assertNote(TsfNote.Length length, TsfNote.Accent accent, int octave, TsfNote.Type type, TsfNote note) {
        assertNotNull(note);
        assertEquals(length, note.getLength());
        assertEquals(accent, note.getAccent());
        assertEquals(octave, note.getOctave());
        assertEquals(type, note.getType());
    }

    @Test
    void min() {
        assertEquals(TsfNote.Length.FULL, TsfTokenParser.min(TsfNote.Length.FULL, TsfNote.Length.FULL));
        assertEquals(TsfNote.Length.HALF, TsfTokenParser.min(TsfNote.Length.HALF, TsfNote.Length.FULL));
        assertEquals(TsfNote.Length.QUARTER, TsfTokenParser.min(TsfNote.Length.HALF, TsfNote.Length.QUARTER));
        assertEquals(TsfNote.Length.THIRD, TsfTokenParser.min(TsfNote.Length.HALF, TsfNote.Length.THIRD));
        assertEquals(TsfNote.Length.EIGHTS, TsfTokenParser.min(TsfNote.Length.HALF, TsfNote.Length.EIGHTS));
    }

    @Test
    void getPlainNote() {
        TsfNote note;
        note = TsfTokenParser.getPlainNote(":d,");
        assertPlainNote(-1, "d",  note);

        note = TsfTokenParser.getPlainNote("r'");
        assertPlainNote(1, "r",  note);

        note = TsfTokenParser.getPlainNote(":d'%s");
        assertPlainNote(1, "d",  note);
        assertEquals("%s",  note.getPostfix());
    }

    private void assertPlainNote(int octave, String note, TsfNote tsfNote) {
        assertNotNull(note);
        assertEquals(TsfNote.Length.UNKNOWN, tsfNote.getLength());
        assertEquals(TsfNote.Accent.UNKNOWN, tsfNote.getAccent());
        assertEquals(octave, tsfNote.getOctave());
        assertEquals(note, tsfNote.getNote());
        assertEquals(TsfNote.Type.NOTE, tsfNote.getType());
    }

}
