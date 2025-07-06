package de.jm.tsfto.model.song;

import de.jm.tsfto.exception.InvalidSongLineRuntimeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.jm.tsfto.model.song.KeyValueLine.getValue;

public class SongModel {
    private long barCount = 1;

    private static final String beginDocument = """
            \\documentclass[a4paper, 12pt]{article}
            \\usepackage[left=1.3cm, right=1.3cm, top=1.3cm, bottom=1.3cm]{geometry}
            \\usepackage{song}
            \\begin{document}
            """;
    private static final String endDocument = "\\end{document}";

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
        List<VerseLine> verseLines = new ArrayList<>();
        for (String line : lines) {
            if (line == null) {
                continue;
            }

            if (line.isEmpty()) {
                if (!scoreLines.isEmpty()) {
                    songLines.add(ScorePart.of(scoreLines));
                    scoreLines = new ArrayList<>();
                }
                if (!verseLines.isEmpty()) {
                    songLines.add(VersePart.of(verseLines));
                    verseLines = new ArrayList<>();
                }
                continue;
            }

            if (VerseLine.matches(line) || !verseLines.isEmpty()) {
                verseLines.add(VerseLine.of(line));
            } else if (KeyValueLine.matches(line)) {
                songLines.add(new KeyValueLine(line));
            } else if (ColsLine.matches(line)) {
                scoreLines.add(new ColsLine(line));
            } else if (SymbolLine.matches(line)) {
                scoreLines.add(new SymbolLine(getValue(line)));
            } else if (NoteLine.matches(line)) {
                scoreLines.add(NoteLine.of(line));
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
        StringBuilder latexBuilder = new StringBuilder();
        latexBuilder.append(beginDocument).append("\n");
        for (Object object : getSongLines()) {
            if (object instanceof VersePart versePart) {
                latexBuilder.append(versePart.toLatex()).append('\n');
            } else if (object instanceof ScorePart scorePart) {
                latexBuilder.append(scorePart.toLatex(barCount)).append('\n');
                barCount += scorePart.getBarCount();
            }  else if (object instanceof SongLine songLine) {
                latexBuilder.append(songLine.toLatex()).append('\n');
            } else {
                throw new RuntimeException("Unknown SongModel object: " + object);
            }
        }
        latexBuilder.append(endDocument).append("\n");
        return latexBuilder.toString();
    }
}
