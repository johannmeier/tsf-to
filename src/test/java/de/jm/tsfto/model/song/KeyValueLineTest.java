package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueLineTest {

    @Test
    void matches() {
        assertTrue(KeyValueLine.matches("C: J.S. Bach"));
        assertTrue(KeyValueLine.matches("bpm: 60"));
        assertFalse(KeyValueLine.matches("bpm is 60:"));
        assertFalse(KeyValueLine.matches(":bpm is 60"));
        assertFalse(KeyValueLine.matches("s: $"));
    }

    @Test
    void getKey() {
        assertEquals("C", KeyValueLine.of("C: J.S. Bach").getKey());
        assertEquals("bpm", KeyValueLine.of("bpm: 60").getKey());
    }

    @Test
    void getValue() {
        assertEquals("J.S. Bach", KeyValueLine.of("C: J.S. Bach").getValue());
        assertEquals("60", KeyValueLine.of("bpm: 60").getValue());
    }

    @Test
    void isKeyValue() {
        assertTrue(KeyValueLine.isKeyValue("b:50"));
        assertTrue(KeyValueLine.isKeyValue("C: J.S. Bach"));
    }
}
