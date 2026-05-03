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
        String processedLine = line;
        String voice ="";
        if (isKeyValue(line)) {
            if ("v".equals(getKey(line))) {
                processedLine = getValue(line);
                int indexOfSpace = processedLine.indexOf(" ");
                voice = processedLine.substring(0, indexOfSpace);
                processedLine = processedLine.substring(indexOfSpace + 1);
            }
        }
        return new NoteLine(processedLine, voice);
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

    private final static String tokenStarts = "!|;:.,`";
    public static boolean matches(String line) {
        if (line == null || line.isEmpty()) {
            return false;
        }

        if (isKeyValue(line)) {
            return line.startsWith("v:");
        }

        String[] tokens = line.split(" +");
        for (String token : tokens) {
            if (token.isEmpty() || !tokenStarts.contains(String.valueOf(token.charAt(0)))) {
                return false;
            }
        }
        return true;
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

    public String getVisibleVoice() {
        if (Character.isUpperCase(voice.charAt(0))) {
            return voice;
        } else {
            return "";
        }
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

}
