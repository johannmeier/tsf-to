package de.jm.tsfto.parser;

import de.jm.tsfto.model.tsf.TsfToken;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TsfParserTest {

    @Test
    void measureToken() {
        List<TsfToken> tokens = new TsfParser("!d !r, !ma'").parse();
        assertEquals("!d", tokens.get(0).toString());
        assertEquals("!r,", tokens.get(1).toString());
        assertEquals("!ma'", tokens.get(2).toString());
    }

    @Test
    void lowNoteThenHalfToken() {
        List<TsfToken> tokens = new TsfParser(":l, ,t, .d, ,r").parse();
        assertEquals(":l,", tokens.get(0).toString());
        assertEquals(",t,", tokens.get(1).toString());
        assertEquals(".d,", tokens.get(2).toString());
        assertEquals(",r", tokens.get(3).toString());
    }

    @Test
    void eigthToken() {
        List<TsfToken> tokens = new TsfParser(";d d ,r r .m ,f").parse();
        assertEquals(";d", tokens.get(0).toString());
        assertEquals("d", tokens.get(1).toString());
        assertEquals(",r", tokens.get(2).toString());
        assertEquals("r", tokens.get(3).toString());
        assertEquals(".m", tokens.get(4).toString());
        assertEquals(",f", tokens.get(5).toString());
    }

    @Test
    void restToken() {
        List<TsfToken> tokens = new TsfParser("! ; : . , /").parse();
        assertEquals("!", tokens.get(0).toString());
        assertEquals(";", tokens.get(1).toString());
        assertEquals(":", tokens.get(2).toString());
        assertEquals(".", tokens.get(3).toString());
        assertEquals(",", tokens.get(4).toString());
        assertEquals("/", tokens.get(5).toString());
    }

    @Test
    void halfQuarterToken() {
        List<TsfToken> tokens = new TsfParser(":d .,r").parse();
        assertEquals(":d", tokens.get(0).toString());
        assertEquals(".,r", tokens.get(1).toString());
    }

    @Test
    void legalIllegalToken() {
        List<TsfToken> tokens = new TsfParser(";d,,d:d.,r").parse();
        assertEquals(";d", tokens.get(0).toString());
        assertEquals(",,d", tokens.get(1).toString());
        assertEquals(":d", tokens.get(2).toString());
        assertEquals(".,r", tokens.get(3).toString());
    }

    @Test
    void otherToken() {
        List<TsfToken> tokens = new TsfParser(":d-s ,,r ;d,_ ,d= !d+r :d* !! *").parse();
        assertEquals(":d-s", tokens.get(0).toString());
        assertEquals(",,r", tokens.get(1).toString());
        assertEquals(";d,_", tokens.get(2).toString());
        assertEquals(",d=", tokens.get(3).toString());
        assertEquals("!d+r", tokens.get(4).toString());
        assertEquals(":d*", tokens.get(5).toString());
        assertEquals("!!", tokens.get(6).toString());
    }

}
