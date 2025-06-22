package de.jm.tsfto.model.song;

public class VerseLine extends SongLine {
    private final boolean startLine;

    private VerseLine(String line, boolean startLine) {
        super(line);
        this.startLine = startLine;
    }

    public static VerseLine of(String line) {
        if (matches(line)) {
            return new VerseLine(KeyValueLine.getValue(line), true);
        } else {
            return new VerseLine(line, false);
        }
    }

    @Override
    public String toLatex() {
        return line;
    }

    public boolean isStartLine() {
        return startLine;
    }

    public static boolean matches(String line) {
        return KeyValueLine.isKeyValue(line) && "verse".equals(KeyValueLine.getKey(line));
    }
}
