package de.jm.tsfto.parser;

import java.util.*;

public class MergeCols {
    private static final Map<String, String> colToCols = new HashMap<>();

    static {
        colToCols.put("FF", "F");
        colToCols.put("FH", "HH");
        colToCols.put("FT", "TTT");
        colToCols.put("FQ", "QQH");
        colToCols.put("FS", "SSSH");
        colToCols.put("FE", "EEQH");
        colToCols.put("HH", "H");
        colToCols.put("HQ", "QQ");
        colToCols.put("HS", "SSS");
        colToCols.put("HE", "EEQ");
        colToCols.put("TT", "T");
        colToCols.put("TS", "SS");
        colToCols.put("QQ", "Q");
        colToCols.put("QE", "EE");
        colToCols.put("SS", "S");
        colToCols.put("EE", "E");
    }



    public static String merge(List<String> columns) {

        List<String> cols = new ArrayList<>(columns);

        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < maxLength(cols); i++) {
                String shortestCol = getShortestCol(cols, i);
                if (replaceCol(cols, i, shortestCol)) {
                    changed = true;
                    break;
                }
            }
        } while (changed);

        return cols.getFirst();
    }

    static int maxLength(List<String> cols) {
        int max = 0;
        for (String col : cols) {
            int length = col.length();
            if (length > max) {
                max = length;
            }
        }
        return max;
    }

    static boolean replaceCol(List<String> cols, int pos, String shortestCol) {
        boolean changed = false;
        for (int i = 0; i < cols.size(); i++) {
            String current = cols.get(i);
            String key = current.charAt(pos) + shortestCol;
            String prefix;
            if (pos == 0) {
                prefix = "";
            } else {
                prefix = current.substring(0, pos);
            }
            String suffix = current.substring(pos + 1);
            String replaced = prefix + colToCols.get(key) + suffix;
            changed |= !current.equals(replaced);
            cols.set(i, replaced);
        }
        return changed;
    }

    static String getShortestCol(List<String> colsList, int pos) {
        Character shortestCol = 'F';
        for (String cols : colsList) {
            Character col = cols.charAt(pos);
            if (compareLength(shortestCol, col) > 0) {
                shortestCol = col;
            }
        }
        return String.valueOf(shortestCol);
    }

    static int compareLength(Character left, Character right) {
        String sortedColsLength = "ESQTHF";

        int leftIndex = sortedColsLength.indexOf(left);
        int rightIndex = sortedColsLength.indexOf(right);

        return Integer.compare(leftIndex, rightIndex);
    }
}
