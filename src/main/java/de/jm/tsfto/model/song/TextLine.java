package de.jm.tsfto.model.song;

public class TextLine extends SongLine {

    public TextLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        return "TODO";
    }

    public static boolean matches(String line) {
        return !KeyValueLine.matches(line) && !NoteLine.matches(line) && !SymbolLine.matches(line);
    }
}
