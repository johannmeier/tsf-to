package de.jm.tsfto.latex;

import de.jm.tsfto.model.tsf.TsfNote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteLineToLatex {

    private static final Map<TsfNote.Length, String> lengthToCol = new HashMap<>();

    static {
        lengthToCol.put(TsfNote.Length.FULL, "F");
        lengthToCol.put(TsfNote.Length.HALF_QUARTER, "HQ");
        lengthToCol.put(TsfNote.Length.HALF, "H");
        lengthToCol.put(TsfNote.Length.TWO_THIRDS, "TT");
        lengthToCol.put(TsfNote.Length.THIRD, "T");
        lengthToCol.put(TsfNote.Length.QUARTER, "Q");
        lengthToCol.put(TsfNote.Length.EIGHTS, "E");
    }

    public static String tsfNotesToLatex(List<TsfNote> notes) {
        StringBuilder cols = new StringBuilder();
        for (TsfNote note : notes) {
            cols.append(lengthToCol.getOrDefault(note.getLength(), ""));
        }
        return cols.toString();
    }
}
