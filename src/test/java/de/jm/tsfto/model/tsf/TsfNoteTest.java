package de.jm.tsfto.model.tsf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TsfNoteTest {

    @Test
    void isStaccato() {
        assertTrue(new TsfNote(0, "d", TsfNote.Length.FULL, TsfNote.Accent.BAR, "!", " . ").isStaccato());
    }
}
