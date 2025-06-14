package de.jm.tsfto.model.song;

import java.util.List;

public class ScorePart {
    private final List<SongLine> songLines;

    private ScorePart(List<SongLine> songLines) {
        this.songLines = songLines;
    }

    public static ScorePart of(List<SongLine> songLines) {
        return new ScorePart(songLines);
    }

    public List<SongLine> getSongLines() {
        return songLines;
    }

    @Override
    public String toString() {
        StringBuilder lines = new StringBuilder();
        for (SongLine songLine : songLines) {
            lines.append(" ").append(songLine.toString()).append("\n");
        }
        return "ScorePart{\n" +
                lines +
                '}';
    }

    public String toLatex() {
        return "TODO";
    }
}
