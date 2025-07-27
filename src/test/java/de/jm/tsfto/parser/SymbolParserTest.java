package de.jm.tsfto.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SymbolParserTest {

    @Test
    void parse() {
        List<String> tokens;

        tokens = SymbolParser.parse(">DS");
        assertEquals(2, tokens.size());
        assertEquals(">", tokens.getFirst());
        assertEquals("DS", tokens.get(1));

        tokens = SymbolParser.parse("cresc_$");
        assertEquals(3, tokens.size());
        assertEquals("cresc", tokens.getFirst());
        assertEquals("_", tokens.get(1));
        assertEquals("$", tokens.get(2));

        tokens = SymbolParser.parse("_");
        assertEquals(1, tokens.size());
        assertEquals("_", tokens.getFirst());

        tokens = SymbolParser.parse("**");
        assertEquals(2, tokens.size());
        assertEquals("*", tokens.getFirst());
        assertEquals("*", tokens.get(1));
    }

    @Test
    void nextToken() {
        SymbolParser parser;
        parser = new SymbolParser(">");
        assertEquals(">", parser.nextToken());

        parser = new SymbolParser(">2.0");
        assertEquals(">2.0", parser.nextToken());

        parser = new SymbolParser(">left");
        assertEquals(">", parser.nextToken());
        assertEquals("left", parser.nextToken());
    }
}
