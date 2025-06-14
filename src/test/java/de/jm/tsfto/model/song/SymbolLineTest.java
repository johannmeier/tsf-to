package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SymbolLineTest {

    @Test
    void matches() {
        assertTrue(SymbolLine.matches("s: * $ DC"));
        assertFalse(SymbolLine.matches("!the sun shines always"));
        assertFalse(SymbolLine.matches("!d .l :s, d . ;d-t :l"));
        assertFalse(SymbolLine.matches("T: !d .l :s, d . ;d-t :l"));
        assertFalse(SymbolLine.matches("C: !the sun shines always"));
    }
}
