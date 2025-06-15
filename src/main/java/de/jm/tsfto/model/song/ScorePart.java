package de.jm.tsfto.model.song;

import de.jm.tsfto.latex.Latex;
import de.jm.tsfto.model.tsf.TsfNote;

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
        StringBuilder latexBuilder = new StringBuilder();

        ColsLine colsLine = getColsLine();
        String cols;

        if (colsLine == null) {
            cols = getCols();
        } else {
            cols = colsLine.toLatex();
        }

        for (SongLine songLine : songLines) {
            if (songLine instanceof NoteLine) {
                NoteLine noteLine = (NoteLine) songLine;
                latexBuilder.append(noteLineToLatex(noteLine, cols)).append('\n');
            }
        }

        return latexBuilder.toString();
    }

    private String noteLineToLatex(NoteLine noteLine, String cols) {
        StringBuilder latexBuilder = new StringBuilder();
        List<TsfNote> notes = noteLine.getTsfNotes();

        if (notes.size() <= cols.length()) {
            throw new RuntimeException("cols size " + cols.length() + " must be larger than notes size " + notes.size());
        }

        for (int i = 0; i < cols.length(); i++) {
            TsfNote note = notes.get(i);
            latexBuilder.append(Latex.tsfNoteToLatex(note));
            if (note.getLength() == TsfNote.Length.HALF_QUARTER || note.getLength() == TsfNote.Length.TWO_THIRDS) {
                latexBuilder.append(" & ");
                i++;
            }
        }

        return latexBuilder.toString();
    }

    private String getCols() {
        return "TODO";
    }

    private ColsLine getColsLine() {
        for (SongLine songLine : songLines) {
            if (songLine instanceof ColsLine) {
                return (ColsLine) songLine;
            }
        }
        return null;
    }
}
