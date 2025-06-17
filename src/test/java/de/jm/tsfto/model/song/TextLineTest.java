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


    @Test
    void toLatex() {
        assertEquals("\\multiline{3}{L}{\\st{hello\\hfill wolfgang}}", new TextLine("hello>wolfgang**").toLatex());
        assertEquals("", new TextLine("").toLatex());
        assertEquals("\\st{hello}", new TextLine("hello").toLatex());
        assertEquals("\\st{hello wolfgang}", new TextLine("hello_wolfgang").toLatex());
        assertEquals("\\st{hello}&\\st{wolfgang}", new TextLine("hello wolfgang").toLatex());
        assertEquals("\\multiline{2}{L}{\\st{hello}}", new TextLine("hello*").toLatex());
        assertEquals("\\multiline{3}{R}{\\st{hello}}", new TextLine("*hello*").toLatex());
        assertEquals("\\multiline{3}{L}{\\st{hello wolfgang}}", new TextLine("hello*wolfgang").toLatex());
        assertEquals("\\ms\\st{hello!}", new TextLine("!hello!").toLatex());
        assertEquals("\\ds\\st{hello!}", new TextLine("!!hello!").toLatex());
        assertEquals("\\st{hello}", new TextLine("hello !!").toLatex());

    }
}
