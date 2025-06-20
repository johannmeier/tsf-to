package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScorePartTest {

    @Test
    void getCols() {
        assertEquals("QQH", ScorePart.getCols(NoteLine.of(":d ,d .s")));
        assertEquals("F", ScorePart.getCols(NoteLine.of("!d")));
        assertEquals("HQQ", ScorePart.getCols(NoteLine.of(":d .,d")));
        assertEquals("TTT", ScorePart.getCols(NoteLine.of(";d //d")));
        assertEquals("HH", ScorePart.getCols(NoteLine.of("|d .d")));
        assertEquals("TTT", ScorePart.getCols(NoteLine.of("!d /d /d")));
        assertEquals("EEQH", ScorePart.getCols(NoteLine.of("!d d ,d .d")));
    }
}
