package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void getBarCount() {
        assertEquals(2, ScorePart.of(List.of(NoteLine.of(":d !d :r :m !d"))).getBarCount());
        assertEquals(2, ScorePart.of(List.of(NoteLine.of("!d :r :m !d"))).getBarCount());
        assertEquals(2, ScorePart.of(List.of(NoteLine.of("!d :r :m !! :s"))).getBarCount());
        assertEquals(1, ScorePart.of(List.of(NoteLine.of("!d :r :m !!"))).getBarCount());
    }

    @Test
    void voice() {
        ScorePart scorePart;
        scorePart = ScorePart.of(List.of(NoteLine.of("!d :r :m !d")));
        NoteLine noteLine = (NoteLine) scorePart.getSongLines().getFirst();
        assertEquals("s", noteLine.getVoice());

        scorePart = ScorePart.of(List.of(NoteLine.of("!d :r :m !d"), NoteLine.of("!d :r :m !d")));
        noteLine = (NoteLine) scorePart.getSongLines().getFirst();
        assertEquals("s", noteLine.getVoice());
        noteLine = (NoteLine) scorePart.getSongLines().get(1);
        assertEquals("a", noteLine.getVoice());

        scorePart = ScorePart.of(List.of(NoteLine.of("!d :r :m !d"), NoteLine.of("!d :r :m !d"), NoteLine.of("!d :r :m !d")));
        noteLine = (NoteLine) scorePart.getSongLines().getFirst();
        assertEquals("s", noteLine.getVoice());
        noteLine = (NoteLine) scorePart.getSongLines().get(1);
        assertEquals("a", noteLine.getVoice());
        noteLine = (NoteLine) scorePart.getSongLines().get(2);
        assertEquals("b", noteLine.getVoice());

        scorePart = ScorePart.of(List.of(NoteLine.of("!d :r :m !d"), NoteLine.of("!d :r :m !d"), NoteLine.of("!d :r :m !d"), NoteLine.of("!d :r :m !d")));
        noteLine = (NoteLine) scorePart.getSongLines().getFirst();
        assertEquals("s", noteLine.getVoice());
        noteLine = (NoteLine) scorePart.getSongLines().get(1);
        assertEquals("a", noteLine.getVoice());
        noteLine = (NoteLine) scorePart.getSongLines().get(2);
        assertEquals("t", noteLine.getVoice());
        noteLine = (NoteLine) scorePart.getSongLines().get(3);
        assertEquals("b", noteLine.getVoice());
    }
}
