package de.jm.tsfto.model.song;

public abstract class SongLine {
    protected String line;

    public SongLine(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }

    public abstract String toLatex();

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "'" + line + '\'' +
                '}';
    }
}
