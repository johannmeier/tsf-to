package de.jm.tsfto.model.song;

import de.jm.tsfto.latex.Latex;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.MergeCols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScorePart {

    private final static String beginTabular = "\\begin{tabular}{%s}\n";
    private final static String endTabular = "\\end{tabular}\n";
    private final static String leftBrace = "\\ldelim\\{{%s}{*}&";
    private final static String rightBrace = "\\rdelim\\}{%s}{*}";
    private final static String rightBars = "\\rdelim\\|{%s}{*}";

    public final static String latexRowEndNewline = "\\\\\n";

    private static final Map<TsfNote.Length, String> lengthToCol = new HashMap<>();

    static {
        lengthToCol.put(TsfNote.Length.FULL, "F");
        lengthToCol.put(TsfNote.Length.HALF_QUARTER, "HQ");
        lengthToCol.put(TsfNote.Length.TWO_THIRDS, "TT");
        lengthToCol.put(TsfNote.Length.HALF, "H");
        lengthToCol.put(TsfNote.Length.THIRD, "T");
        lengthToCol.put(TsfNote.Length.QUARTER, "Q");
        lengthToCol.put(TsfNote.Length.EIGHTS, "E");
    }

    private static final Map<Character, Integer> colToInt = new HashMap<>();

    static {
        colToInt.put('F', 24);
        colToInt.put('H', 12);
        colToInt.put('T', 8);
        colToInt.put('Q', 6);
        colToInt.put('S', 4);
        colToInt.put('E', 3);
    }

    private static final Map<TsfNote.Length, Integer> lengthToInt = new HashMap<>();

    static {
        lengthToInt.put(TsfNote.Length.FULL, 24);
        lengthToInt.put(TsfNote.Length.HALF_QUARTER, 18);
        lengthToInt.put(TsfNote.Length.TWO_THIRDS, 16);
        lengthToInt.put(TsfNote.Length.HALF, 12);
        lengthToInt.put(TsfNote.Length.THIRD, 8);
        lengthToInt.put(TsfNote.Length.QUARTER, 6);
        lengthToInt.put(TsfNote.Length.SIXTH, 4);
        lengthToInt.put(TsfNote.Length.EIGHTS, 3);
    }

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

        int countSymbolAndColsLinesAtBeginning = getCountSymbolAndColsLinesAtBeginning();
        int countBracedLines = songLines.size() - countSymbolAndColsLinesAtBeginning;

        boolean isEndRow = isEndRowAndRemoveEndSigns();
        boolean firstBracketLine = true;

        ColsLine colsLine = getColsLine();
        String cols;

        boolean autoCols = true;

        if (colsLine == null) {
            cols = "B " + getCols() + " B";
        } else {
            cols = "B " + colsLine.toLatex() + " B";
            autoCols = false;
        }

        for (SongLine songLine : songLines) {
            if (songLine instanceof SymbolLine symbolLine) {
                latexBuilder.append("&").append(symbolLine.toLatex()).append(latexRowEndNewline);
            }
            if (songLine instanceof NoteLine noteLine) {
                if (firstBracketLine) {
                    latexBuilder.append(leftBrace.formatted(countBracedLines))
                            .append(noteLineToLatex(noteLine, cols, autoCols))
                            .append(isEndRow ? rightBars.formatted(countBracedLines) : rightBrace.formatted(countBracedLines))
                            .append(latexRowEndNewline);
                    firstBracketLine = false;
                } else {
                    latexBuilder.append("&").append(noteLineToLatex(noteLine, cols, autoCols)).append(latexRowEndNewline);
                }
            }
            if (songLine instanceof TextLine textLine) {
                latexBuilder.append("&").append(textLine.toLatex()).append(latexRowEndNewline);
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

    private String noteLineToLatex(NoteLine noteLine, String cols, boolean autoCols) {
        cols = cols.replace(" ", "").replace("B", "");
        StringBuilder latexBuilder = new StringBuilder();
        List<TsfNote> notes = noteLine.getTsfNotes();

        int colTime = 0;
        int noteTime = 0;
        int notePos = 0;

        if (notes.size() > cols.length()) {
            System.out.println(cols);
            System.out.println(notes);
            throw new RuntimeException("cols size " + cols.length() + " must be larger than notes size " + notes.size());
        }

        if (autoCols) {
            TsfNote note;
            int noteColCount = 1;
            for (char c : cols.toCharArray()) {
                if (colTime < noteTime) {
                    if (noteColCount == 1) {
                        latexBuilder.append("&");
                    } else {
                        noteColCount--;
                    }
                } else {
                    note = notes.get(notePos++);
                    noteColCount = note.getColCount();
                    latexBuilder.append(Latex.tsfNoteToLatex(note)).append("&");
                    noteTime += lengthToInt.get(note.getLength());
                }

                colTime += colToInt.get(c);
            }
        } else {
            for (TsfNote note : notes) {
                latexBuilder.append(Latex.tsfNoteToLatex(note)).append("&");
            }
        }


        return latexBuilder.toString();
    }

    private String getCols() {
        List<String> cols = new ArrayList<>();
        for (SongLine songLine : songLines) {
            if (songLine instanceof NoteLine noteLine) {
                cols.add(getCols(noteLine));
            }
        }
        return MergeCols.merge(cols);
    }


    static String getCols(NoteLine noteLine) {
        StringBuilder cols = new StringBuilder();

        for (TsfNote note : noteLine.getTsfNotes()) {
            cols.append(lengthToCol.get(note.getLength()));
        }

        return cols.toString();
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
