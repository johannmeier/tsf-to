package de.jm.tsfto.parser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MergeColsTest {

    @Test
    void compareLength() {
        assertEquals(0, MergeCols.compareLength('F', 'F'));
        assertEquals(1, MergeCols.compareLength('F', 'H'));
        assertEquals(-1, MergeCols.compareLength('Q', 'T'));
    }

    @Test
    void replaceCol() {
        List<String> cols;
        cols = new ArrayList<>(List.of("FF", "FF"));
        assertFalse(MergeCols.replaceCol(cols, 0, "F"));

        cols = new ArrayList<>(List.of("FF", "HHF"));
        assertTrue(MergeCols.replaceCol(cols, 0, "H"));
        assertEquals("HHF", cols.getFirst());

        cols = new ArrayList<>(List.of("FF", "QQF"));
        assertTrue(MergeCols.replaceCol(cols, 0, "Q"));
        assertEquals("QQHF", cols.getFirst());
    }

    @Test
    void merge() {
        List<String> cols;

        cols = new ArrayList<>(List.of("FFFF", "TTTFFF"));
        assertEquals("TTTFFF", MergeCols.merge(cols));

        cols = new ArrayList<>(List.of("FFFF", "TTTFFF", "SSSSSSFFF"));
        assertEquals("SSSSSSFFF", MergeCols.merge(cols));

        cols = new ArrayList<>(List.of("FFFF", "FHHQQHF"));
        assertEquals("FHHQQHF", MergeCols.merge(cols));

        cols = new ArrayList<>(List.of("FFFF", "FHHQQHF", "FFHHF"));
        assertEquals("FHHQQHF", MergeCols.merge(cols));


        cols = new ArrayList<>(List.of("FFFF", "TTTFFF"));
        assertEquals("TTTFFF", MergeCols.merge(cols));

    }

}
