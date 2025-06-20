package de.jm.tsfto.model.song;

import de.jm.tsfto.latex.Latex;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.TsfTokenParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteLine extends SongLine {

    private static final Map<String, Integer> colToInt = new HashMap<>();

    static {
        colToInt.put("F", 24);
        colToInt.put("H", 12);
        colToInt.put("T", 8);
        colToInt.put("Q", 6);
        colToInt.put("S", 4);
        colToInt.put("E", 3);
    }

    private static final Map<TsfNote.Length, Integer> lenghtToInt = new HashMap<>();

    static {
        lenghtToInt.put(TsfNote.Length.FULL, 24);
        lenghtToInt.put(TsfNote.Length.HALF_QUARTER, 18);
        lenghtToInt.put(TsfNote.Length.TWO_THIRDS, 16);
        lenghtToInt.put(TsfNote.Length.HALF, 12);
        lenghtToInt.put(TsfNote.Length.THIRD, 8);
        lenghtToInt.put(TsfNote.Length.QUARTER, 6);
        lenghtToInt.put(TsfNote.Length.SIXTH, 4);
        lenghtToInt.put(TsfNote.Length.EIGHTS, 3);
    }

    public NoteLine(String line) {
        super(line);
    }

    public static NoteLine of(String line) {
        return new NoteLine(line);
    }

    @Override
    public String toLatex() {
        StringBuilder latexBuilder = new StringBuilder();
        List<TsfNote> tsfNotes = getTsfNotes();
        for (int i = 0; i < tsfNotes.size(); i++) {
            TsfNote tsfNote = tsfNotes.get(i);
            if (tsfNote.isTwoNotesOneColumn()) {
                latexBuilder.append(Latex.twoNotesOneColumnToLatex(tsfNote, tsfNotes.get(i++)));
            } else {
                latexBuilder.append(Latex.tsfNoteToLatex(tsfNote));
            }
        }
        return latexBuilder.toString();
    }

    public static boolean matches(String line) {
        final String validChars = "drmfsltaeib_=-+*0123456789!|;:.,/'? ";
        for (char c : line.toCharArray()) {
            if (validChars.indexOf(c) < 0) {
                return false;
            }
        }

        int colonCount = getCount(':', line);
        int bangCount = getCount('!', line);
        return colonCount > 0 && (colonCount + bangCount > 1);
    }

    public static int getCount(char ch, String line) {
        if (line == null) {
            return 0;
        }

        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }

    public List<TsfNote> getTsfNotes() {
        return TsfTokenParser.parse(getTokens(line));
    }

    public static List<String> getTokens(String line) {
        return List.of(line.split(" +"));
    }
}
