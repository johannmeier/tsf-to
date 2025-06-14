package de.jm.tsfto.model.song;

public class SymbolLine extends SongLine {

    public SymbolLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        return "TODO";
    }

    public static boolean matches(String line) {
        return line.startsWith("s:");
    }
}
