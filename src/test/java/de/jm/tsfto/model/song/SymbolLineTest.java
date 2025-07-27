package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void getColCount() {
        assertEquals(1, SymbolLine.getColCount("DC"));
        assertEquals(1, SymbolLine.getColCount("*"));
        assertEquals(1, SymbolLine.getColCount("$_DC"));
        assertEquals(2, SymbolLine.getColCount("**"));
        assertEquals(2, SymbolLine.getColCount("*$"));
        assertEquals(2, SymbolLine.getColCount("$*"));
        assertEquals(3, SymbolLine.getColCount("$>*DC"));
    }

    @Test
    void processToken() {
        assertEquals("", SymbolLine.processToken("*"));
        assertEquals("DC", SymbolLine.processToken("DC"));
        assertEquals("DC", SymbolLine.processToken("*DC"));
        assertEquals("DC", SymbolLine.processToken("DC****"));
        assertEquals("DC_$", SymbolLine.processToken("DC_$****"));
        assertEquals("DC_$", SymbolLine.processToken("DC*$****"));
        assertEquals("DC>$", SymbolLine.processToken("*DC>$****"));
    }

    @Test
    void fillSymbols() {
        assertEquals("\\mnbr{50}", SymbolLine.fillSymbols("b:50"));
        assertEquals("\\tpart{A}", SymbolLine.fillSymbols("p:A"));
        assertEquals("\\first{\\flc*\\real{4}}", SymbolLine.fillSymbols("1.4"));
        assertEquals("\\first[\\rdc]{\\flc*\\real{4}}", SymbolLine.fillSymbols("1.4_dc"));
        assertEquals("\\first[\\rds]{\\flc*\\real{4}}", SymbolLine.fillSymbols("1.4_ds"));
    }

    @Test
    void toLatex() {
        assertEquals("&&&", new SymbolLine("* * *").toLatex());
        assertEquals("\\tpart{A}&&\\sign{hello}&", new SymbolLine("p:A * hello").toLatex());
        assertEquals("\\tpart{A}\\ \\fer&", new SymbolLine("p:A_^").toLatex());
        assertEquals("\\hfill\\coda&", new SymbolLine(">$").toLatex());
        assertEquals("", new SymbolLine("").toLatex());
        assertEquals("&", new SymbolLine("*").toLatex());
        assertEquals("&&", new SymbolLine("**").toLatex());
        assertEquals("\\multicolumn{2}{R}{\\coda}&", new SymbolLine("*$").toLatex());
        assertEquals("\\multicolumn{2}{L}{\\coda}&", new SymbolLine("$*").toLatex());
        assertEquals("\\multicolumn{3}{L}{\\coda\\ \\DC}&", new SymbolLine("$*DC").toLatex());
        assertEquals("\\multicolumn{3}{L}{\\coda\\ \\hfill\\DC}&", new SymbolLine("$*>DC").toLatex());
        assertEquals("&\\fer&", new SymbolLine("* ^").toLatex());
        assertEquals("&&\\fer&", new SymbolLine("** ^").toLatex());
        assertEquals("&\\multicolumn{2}{R}{\\DC}&", new SymbolLine("* *DC").toLatex());
        assertEquals("&\\fer&&", new SymbolLine("* ^ *").toLatex());
    }

    @Test
    void getKeyValues() {
        List<String> keyValues = SymbolLine.getKeyValues("hurz:50");
        assertEquals(0, keyValues.size());

        keyValues = SymbolLine.getKeyValues("b:50");
        assertEquals(1, keyValues.size());
        assertEquals("b:50", keyValues.getFirst());

        keyValues = SymbolLine.getKeyValues("p:A");
        assertEquals(1, keyValues.size());
        assertEquals("p:A", keyValues.getFirst());

        keyValues = SymbolLine.getKeyValues("key:A");
        assertEquals(1, keyValues.size());
        assertEquals("key:A", keyValues.getFirst());

        keyValues = SymbolLine.getKeyValues("b:50_key:A");
        assertEquals(2, keyValues.size());
        assertEquals("b:50", keyValues.getFirst());
        assertEquals("key:A", keyValues.get(1));
    }

    @Test
    void processMultiCols() {
        assertEquals("* * hello", SymbolLine.processMultiCols("*2 hello"));
        assertEquals("* * ", SymbolLine.processMultiCols("*2"));
        assertEquals("*", SymbolLine.processMultiCols("*"));
        assertEquals("* *", SymbolLine.processMultiCols("* *"));
    }

    @Test
    void replaceWedges() {
        assertEquals("\\crescWedge{\\flc*1}", SymbolLine.replaceWedges("<"));
        assertEquals("\\crescWedge{\\flc*4}", SymbolLine.replaceWedges("<4"));
        assertEquals("\\crescWedge{\\flc*1.5}a\\decrescWedge{\\flc*2}", SymbolLine.replaceWedges("<1.5a>2"));
        assertEquals("left>right", SymbolLine.replaceWedges("left>right"));
    }
}
