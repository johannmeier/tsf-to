package de.jm.tsfto.model.song;

import de.jm.tsfto.latex.Latex;
import de.jm.tsfto.model.tsf.TsfNote;

import java.util.List;

public class ScorePart {

    private final static String beginTabular = "\\begin{tabular}{%s}\n";
    private final static String endTabular = "\\end{tabular}\n";
    private final static String leftBrace= "\\ldelim\\{{%s}{*}&";
    private final static String rightBrace= "\\rdelim\\{{%s}{*}";
    private final static String rightBars= "\\rdelim\\|{%s}{*}";



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
            cols = "B " + colsLine.toLatex() + " B";
        }

        int countSymbolAndColsLinesAtBeginning = getCountSymbolAndColsLinesAtBeginning();
        int countBracedLines = songLines.size() - countSymbolAndColsLinesAtBeginning;

        boolean isEndRow = isEndRowAndRemoveEndSigns();
        boolean firstBracketLine = true;

        for (SongLine songLine : songLines) {
            if (songLine instanceof SymbolLine symbolLine) {
                latexBuilder.append("&").append(symbolLine.toLatex()).append("\\\\\n");
            }
            if (songLine instanceof NoteLine noteLine) {
                if (firstBracketLine) {
                    latexBuilder.append(leftBrace.formatted(countBracedLines))
                            .append(noteLineToLatex(noteLine, cols))
                            .append(isEndRow ? rightBars.formatted(countBracedLines) : rightBrace.formatted(countBracedLines))
                            .append("\\\\\n");
                    firstBracketLine = false;
                } else {
                    latexBuilder.append("&").append(noteLineToLatex(noteLine, cols)).append("\\\\\n");
                }
            }
            if (songLine instanceof TextLine textLine) {
                latexBuilder.append("&").append(textLine.toLatex()).append("\\\\\n");
            }
        }

        return beginTabular.formatted(cols) + latexBuilder + endTabular;
    }

    private int getCountSymbolAndColsLinesAtBeginning() {
        int count = 0;
        for (SongLine songLine : songLines) {
            if (songLine instanceof SymbolLine || songLine instanceof ColsLine) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private boolean isEndRowAndRemoveEndSigns() {
        boolean isEndRow = false;
        for (SongLine songLine : songLines) {
            if (songLine.isEndRow()) {
                isEndRow = true;
                songLine.removeEndRowSigns();
            }
        }
        return isEndRow;
    }

    private String noteLineToLatex(NoteLine noteLine, String cols) {
        StringBuilder latexBuilder = new StringBuilder();
        List<TsfNote> notes = noteLine.getTsfNotes();

        if (notes.size() > cols.length()) {
            throw new RuntimeException("cols size " + cols.length() + " must be larger than notes size " + notes.size());
        }

        for (TsfNote note : notes) {
            latexBuilder.append(Latex.tsfNoteToLatex(note)).append("&");
            if (note.getLength() == TsfNote.Length.HALF_QUARTER || note.getLength() == TsfNote.Length.TWO_THIRDS) {
                latexBuilder.append(" & ");
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
