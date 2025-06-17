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

    public boolean isEndRow() {
        return line != null && (line.endsWith("!!") || line.endsWith("||"));
    }

    public void removeEndRowSigns() {
        if (isEndRow()) {
            line = line.replaceFirst(" *[|!][|!]$", "");
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "'" + line + '\'' +
                '}';
    }
}
