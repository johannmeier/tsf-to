package de.jm.tsfto.model.song;

import static de.jm.tsfto.model.song.KeyValueLine.getValue;

public class ColsLine extends SongLine {

    public ColsLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        return getValue(line);
    }

    public static boolean matches(String line) {
        return line.startsWith("cols:");
    }
}
