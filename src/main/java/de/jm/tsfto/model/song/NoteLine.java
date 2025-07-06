package de.jm.tsfto.model.song;

import de.jm.tsfto.latex.Latex;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.TsfTokenParser;

import java.util.List;

import static de.jm.tsfto.model.song.KeyValueLine.*;

public class NoteLine extends SongLine {

    private String voice;

    private NoteLine(String line, String voice) {
        super(line);
        this.voice = voice;
    }

    public static NoteLine of(String line) {
        String voice ="";
        if (isKeyValue(line)) {
            if ("v".equals(getKey(line))) {
                String processedLine = getValue(line);
                voice = processedLine.substring(0, processedLine.indexOf(' '));
            }
        }
        return new NoteLine(line, voice);
    }

    public static NoteLine of(String line, String voice) {
        return new NoteLine(line, voice);
    }

    @Override
    // TODO: not used in ScorePart
    public String toLatex() {
        StringBuilder latexBuilder = new StringBuilder();
        List<TsfNote> tsfNotes = getTsfNotes();
        for (int i = 0; i < tsfNotes.size(); i++) {
            TsfNote tsfNote = tsfNotes.get(i);
            if (tsfNote.isTwoNotesOneColumn()) {
                latexBuilder.append(Latex.twoNotesMultiColumnToLatex(tsfNote, tsfNotes.get(i++)));
            } else {
                latexBuilder.append(Latex.tsfNoteToLatex(tsfNote));
            }
        }
        return latexBuilder.toString();
    }

    public static boolean matches(String line) {
        if (isKeyValue(line)) {
            return line.startsWith("v:");
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

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

}
