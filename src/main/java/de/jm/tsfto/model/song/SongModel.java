package de.jm.tsfto.model.song;

import de.jm.tsfto.exception.InvalidSongLineRuntimeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SongModel {
    private final List<Object> songLines;

    public SongModel(List<Object> songLines) {
        this.songLines = songLines;
    }

    public static SongModel parse(String filename) {
        try {
            return parse(Files.readAllLines(Path.of(filename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SongModel parse(List<String> lines) {
        List<Object> songLines = new ArrayList<>();
        List<SongLine> scoreLines = new ArrayList<>();
        for (String line : lines) {
            if (line == null) {
                continue;
            }

            if (line.isEmpty()) {
                if (!scoreLines.isEmpty()) {
                    songLines.add(ScorePart.of(scoreLines));
                    scoreLines = new ArrayList<>();
                }
                continue;
            }

            if (KeyValueLine.matches(line)) {
                songLines.add(new KeyValueLine(line));
            } else if (ColsLine.matches(line)) {
                scoreLines.add(new ColsLine(line));
            } else if (SymbolLine.matches(line)) {
                scoreLines.add(new SymbolLine(line));
            } else if (NoteLine.matches(line)) {
                scoreLines.add(new NoteLine(line));
            } else if (TextLine.matches(line)) {
                scoreLines.add(new TextLine(line));
            } else {
                throw new InvalidSongLineRuntimeException(line);
            }
        }

        if (!scoreLines.isEmpty()) {
            songLines.add(ScorePart.of(scoreLines));
        }

        return new SongModel(songLines);
    }

    public List<Object> getSongLines() {
        return songLines;
    }

    public String toLatex() {
        StringBuilder sb = new StringBuilder();

        for (Object line : songLines) {
            if (line instanceof SongLine) {
                sb.append(((SongLine) line).toLatex());
            }
            if (line instanceof ScorePart) {
                sb.append(((ScorePart) line).toLatex());
            }
        }

        return sb.toString();
    }
}
