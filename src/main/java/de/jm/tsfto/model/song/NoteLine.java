package de.jm.tsfto.model.song;

import de.jm.tsfto.latex.Latex;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.TsfTokenParser;

import java.util.List;

public class NoteLine extends SongLine {


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
        if (line.startsWith("t:") || line.startsWith("s:")) {
            return false;
        }

        final String validChars = "drmfsltaeib_=-+*0123456789!|;:.,/'?^% ";
        for (char c : line.toCharArray()) {
            if (validChars.indexOf(c) < 0) {
                return false;
            }
        }

        int colonCount = getCountTokenStartingWith(':', line);
        int bangCount = getCountTokenStartingWith('!', line);
        bangCount += getCountTokenStartingWith('|', line);
        int semicolonCount = getCountTokenStartingWith(';', line);
        return getCountTokenStartingWith('*', line) == 0 & (colonCount + semicolonCount + bangCount > 1);
    }

    public static int getCountTokenStartingWith(char ch, String line) {
        if (line == null) {
            return 0;
        }

        int count = 0;
        for (String token : line.split(" ")) {
            if (token.startsWith(String.valueOf(ch))) {
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
