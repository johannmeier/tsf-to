package de.jm.tsfto.model.export;

import de.jm.tsfto.model.tsf.TsfNote;
import org.junit.jupiter.api.Test;


import static de.jm.tsfto.model.tsf.TsfNote.Length.FULL;
import static de.jm.tsfto.model.tsf.TsfNote.Length.HALF;
import static org.junit.jupiter.api.Assertions.*;

class TsfToWesternNoteTest {


    @Test
    void getNote() {
        assertEquals("c", TsfToWesternNote.getNote("d", "C"));
        assertEquals("ces", TsfToWesternNote.getNote("da", "C"));
        assertEquals("d", TsfToWesternNote.getNote("d", "D"));
        assertEquals("bes", TsfToWesternNote.getNote("d", "Bes"));
        assertEquals("d", TsfToWesternNote.getNote("f", "A"));
    }

    @Test
    void getPitch() {
        assertEquals(4, TsfToWesternNote.getPitch(0, "C", "d", true));
        assertEquals(3, TsfToWesternNote.getPitch(0, "C", "d", false));
        assertEquals(3, TsfToWesternNote.getPitch(0, "D", "d", false));
        assertEquals(4, TsfToWesternNote.getPitch(0, "G", "f", false));
    }

    @Test
    void tsfToWesternNote() {
        TsfNote tsfNote = new TsfNote(0, "d", FULL, TsfNote.Accent.NONE, ":", "");
        WesternNote westernNote = TsfToWesternNote.tsfToWesternNote(tsfNote, "C", true);
        WesternNote expectedWesternNote = new WesternNote("c", 4, FULL, tsfNote.getAccent(), tsfNote.getType());
        assertEquals(expectedWesternNote, westernNote);

        tsfNote = new TsfNote(0, "f", HALF, TsfNote.Accent.UNKNOWN, ".", "");
        westernNote = TsfToWesternNote.tsfToWesternNote(tsfNote, "G", true);
        expectedWesternNote = new WesternNote("c", 5, HALF, TsfNote.Accent.UNKNOWN, TsfNote.Type.NOTE);
        assertEquals(expectedWesternNote, westernNote);

        tsfNote = new TsfNote(0, "", HALF, TsfNote.Accent.UNKNOWN, ".", "");
        westernNote = TsfToWesternNote.tsfToWesternNote(tsfNote, "G", true);
        expectedWesternNote = new WesternNote("", 0, HALF, TsfNote.Accent.UNKNOWN, TsfNote.Type.BREAK);
        assertEquals(expectedWesternNote, westernNote);
    }

    @Test
    void addTsfLength() {
        WesternNote westernNote = new WesternNote("c", 5, HALF, TsfNote.Accent.UNKNOWN, TsfNote.Type.NOTE);
        westernNote.addTsfLength(FULL);
        assertEquals(0.375f, westernNote.getLengthInWholes());
        westernNote.addTsfLength(HALF);
        westernNote.addTsfLength(FULL);
        westernNote.addTsfLength(FULL);
        assertEquals(1.0f, westernNote.getLengthInWholes());
    }
}
