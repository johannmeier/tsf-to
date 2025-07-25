package de.jm.tsfto.model.song;

import de.jm.tsfto.latex.Latex;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.parser.MergeCols;
import de.jm.tsfto.parser.TsfTokenParser;

import java.util.*;

public class ScorePart {
    private static int countScorePart = 1;

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
        setVoices();
    }

    private void setVoices() {
        long countOfNoteLines = songLines.stream().filter(songLine -> songLine instanceof NoteLine).count();
        int pos = 0;
        for (SongLine songLine : songLines) {
            if (songLine instanceof NoteLine noteLine) {
                if (noteLine.getVoice().isEmpty()) {
                    noteLine.setVoice(getVoice(pos, countOfNoteLines));
                }
                pos++;
            }
        }
    }

    private static String getVoice(int pos, long countOfNoteLines) {
        if (countOfNoteLines == 1) {
            return "s";
        }
        if (countOfNoteLines == 2) {
            return pos == 0 ? "s" : "a";
        }

        if (countOfNoteLines == 3) {
            return pos == 0 ? "s" : (pos == 1 ? "a" : "b");
        }

        if (countOfNoteLines == 4) {
            return pos == 0 ? "s" : (pos == 1 ? "a" : (pos == 2 ? "t" : "b"));
        }

        return "";
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

    public String toLatex(long barCount) {
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

        if (hasVoice()) {
            cols = "l" + cols;
        }

        for (int i = 0; i < songLines.size(); i++) {
            StringBuilder latexLine = new StringBuilder();
            if (hasVoice()) {
                latexLine.append("&");
            }

            SongLine songLine = songLines.get(i);
            if (songLine instanceof SymbolLine symbolLine) {
                if (countScorePart % 2 == 0 && (i + 1) < songLines.size() && songLines.get(i + 1) instanceof NoteLine) {
                    latexLine.append("\\mnbr{%s}\\ ".formatted(barCount));
                }
                latexLine.append("&").append(symbolLine.toLatex()).append(latexRowEndNewline);
            }

            if (songLine instanceof NoteLine noteLine) {
                if (hasVoice()) {
                    latexLine.insert(0, noteLine.getVisibleVoice());
                }
                if (firstBracketLine) {
                    if (countScorePart % 2 == 0 && (i == 0 || !(songLines.get(i - 1) instanceof SymbolLine))) {
                        latexBuilder.append("&\\mnbr{%s}\\ ".formatted(barCount)).append(latexRowEndNewline);
                    }
                    latexLine.append(leftBrace.formatted(countBracedLines))
                            .append(noteLineToLatex(noteLine, hasVoice() ? cols.substring(1) : cols, autoCols))
                            .append(isEndRow ? rightBars.formatted(countBracedLines) : rightBrace.formatted(countBracedLines))
                            .append(latexRowEndNewline);
                    firstBracketLine = false;
                } else {
                    latexLine.append("&").append(noteLineToLatex(noteLine, hasVoice() ? cols.substring(1) : cols, autoCols)).append(latexRowEndNewline);
                }
            }
            if (songLine instanceof TextLine textLine) {
                latexLine.append("&").append(textLine.toLatex()).append(latexRowEndNewline);
            }
            latexBuilder.append(latexLine);
        }

        countScorePart++;
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
            for (int i = 0; i < cols.length(); i++) {
                char c = cols.charAt(i);
                TsfNote nextNote = (notePos + 1 < notes.size()) ? notes.get(notePos + 1) : null;
                TsfNote previousNote = (notePos -1 >=  0) ? notes.get(notePos - 1) : null;

                if (colTime < noteTime) {
                    if (noteColCount == 1) {
                        latexBuilder.append("&");
                    } else {
                        noteColCount--;
                    }
                } else {
                    note = notes.get(notePos++);
                    noteColCount = note.getColCount();
                    if (note.isTwoNotesOneColumn()) {
                        latexBuilder.append(Latex.twoNotesMultiColumnToLatex(note, nextNote)).append("&");
                    } else if (note.isStack()) {
                        TsfNote secondNote = TsfTokenParser.getPlainNote(note.getSecondNote());
                        String space = "1px";
                        latexBuilder.append(Latex.prefixToPlainLatex.get(note.getPrefix()));
                        latexBuilder.append("\\lstack[%s]{%s}{%s}&".formatted(space, Latex.getPlainNoteLatex(note), Latex.getPlainNoteLatex(secondNote)));
                    } else {
                        if (previousNote == null || !previousNote.isTwoNotesOneColumn()) {
                            latexBuilder.append(Latex.tsfNoteToLatex(note)).append((note.getLength() == TsfNote.Length.HALF_QUARTER && nextNote != null && !nextNote.isHalfHalfQuarter()) ? "" : "&");
                        }
                    }
                    if (note.isHalfHalfQuarter() || (nextNote != null && nextNote.isHalfHalfQuarter())) {
                        noteTime += lengthToInt.get(TsfNote.Length.HALF);
                    } else {
                        noteTime += lengthToInt.get(note.getLength());
                    }
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

        List<TsfNote> notes = noteLine.getTsfNotes();
        for (int i = 0; i < notes.size(); i++) {
            TsfNote note = notes.get(i);
            TsfNote nextNote = (i + 1 == notes.size()) ? null : notes.get(i + 1);
            if (note.isHalfHalfQuarter() || (nextNote != null && nextNote.isHalfHalfQuarter())) {
                cols.append(lengthToCol.get(TsfNote.Length.HALF));
            } else {
                cols.append(lengthToCol.get(note.getLength()));
            }
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

    public long getBarCount() {
        for (SongLine songLine : songLines) {
            if (songLine instanceof NoteLine noteLine) {
                String[] parts = noteLine.getLine().split("[!|]+");
                return Arrays.stream(parts).filter(p -> !p.isEmpty()).count();
            }
        }
        return 0;
    }

    private boolean hasVoice() {
        for (SongLine songLine : songLines) {
            if (songLine instanceof NoteLine noteLine) {
                String voice = noteLine.getVoice();
                if (voice != null && Character.isUpperCase(voice.charAt(0))) {
                    return true;
                }
            }
        }
        return false;
    }
}
