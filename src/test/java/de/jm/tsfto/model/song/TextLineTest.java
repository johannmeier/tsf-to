package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextLineTest {

    @Test
    void matches() {
        assertTrue(TextLine.matches("!the sun shines always"));
        assertFalse(TextLine.matches("s: !the sun shines always"));
        assertFalse(TextLine.matches("!d .l :s, d . ;d-t :l"));
        assertFalse(TextLine.matches("T: !d .l :s, d . ;d-t :l"));
        assertFalse(TextLine.matches("C: !the sun shines always"));
    }
}
