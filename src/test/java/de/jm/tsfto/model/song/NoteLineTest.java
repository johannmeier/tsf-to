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

    @Test
    void voice() {
        assertEquals("", NoteLine.of("!d .l :s, d . :l").getVoice());
        assertEquals("t", NoteLine.of("v:t !d .l :s, d . :l").getVoice());
        assertEquals("s", NoteLine.of("v: s !d .l :s, d . :l").getVoice());
        assertEquals("a1", NoteLine.of("v: a1 !d .l :s, d . :l").getVoice());
    }
}
