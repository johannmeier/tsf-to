package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueLineTest {

    @Test
    void matches() {
        assertTrue(KeyValueLine.matches("C: J.S. Bach"));
        assertTrue(KeyValueLine.matches("Pulse: 60"));
        assertFalse(KeyValueLine.matches("Pulse is 60:"));
        assertFalse(KeyValueLine.matches(":Pulse is 60"));
        assertFalse(KeyValueLine.matches("s: $"));
    }

    @Test
    void getKey() {
        assertEquals("C", KeyValueLine.of("C: J.S. Bach").getKey());
        assertEquals("Pulse", KeyValueLine.of("Pulse: 60").getKey());
    }

    @Test
    void getValue() {
        assertEquals("J.S. Bach", KeyValueLine.of("C: J.S. Bach").getValue());
        assertEquals("60", KeyValueLine.of("Pulse: 60").getValue());
    }
}
