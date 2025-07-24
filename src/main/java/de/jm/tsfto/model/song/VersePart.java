package de.jm.tsfto.model.song;

import java.util.ArrayList;
import java.util.List;

import static de.jm.tsfto.model.song.ScorePart.latexRowEndNewline;

public class VersePart {

    private final List<VerseLine> songLines;

    private VersePart(List<VerseLine> songLines) {
        this.songLines = songLines;
    }

    public static VersePart of(List<VerseLine> songLines) {
        return new VersePart(songLines);
    }

    public String toLatex() {
        StringBuilder latex = new StringBuilder();

        StringBuilder currentVerse = new StringBuilder();
        List<String> verses = new ArrayList<>();
        for (VerseLine verseLine : songLines) {
            if (!currentVerse.isEmpty()) {
                currentVerse.append(latexRowEndNewline);
            }
            if (verseLine.isStartLine() && !currentVerse.isEmpty()) {
                verses.add(currentVerse.toString());
                currentVerse.setLength(0);
            }
            currentVerse.append(verseLine.toLatex());
        }

        if (!currentVerse.isEmpty()) {
            verses.add(currentVerse.toString());
        }

        if (verses.size() == 2) {
            latex.append("\\parbox{\\textwidth/2-0.25cm}{\n");
            latex.append(verses.getFirst()).append("\n");
            latex.append("}\n");
            latex.append("\\hspace{0.25cm}\n");
            latex.append("\\parbox{\\textwidth/2-0.25cm}{\n");
            latex.append(verses.get(1)).append("\n");
            latex.append("}\n");
        } else {
            latex.append(verses.getFirst()).append("\n");
        }

        return latex.toString();
    }
}
