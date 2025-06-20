package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteLineTest {

    @Test
    void matches() {
        assertTrue(NoteLine.matches("!d .l :s, d . ;d-t :l"));
        assertFalse(NoteLine.matches("T: !d .l :s, d . ;d-t :l"));
        assertFalse(NoteLine.matches("!the sun shines always"));
        assertFalse(NoteLine.matches("s: !the sun shines always"));
        assertFalse(NoteLine.matches("C: !the sun shines always"));
    }

    @Test
    void toLatex() {
        assertEquals("\\multicolumn{3}{L}{\\nn{d}}", NoteLine.of(":d**").toLatex());
    }

}
