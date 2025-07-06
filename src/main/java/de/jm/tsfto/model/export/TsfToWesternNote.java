package de.jm.tsfto.model.export;

import de.jm.tsfto.model.tsf.TsfNote;

import java.util.HashMap;
import java.util.Map;

public class TsfToWesternNote {

    private static final Map<String, String[]> keyToWesternNote = new HashMap<>();

    static {
        keyToWesternNote.put("Ges", new String[]{"ges", "aes", "bes", "ces", "des", "ees", "f"});
        keyToWesternNote.put("Des", new String[]{"des", "ees", "f", "ges", "aes", "bes", "c"});
        keyToWesternNote.put("Aes", new String[]{"aes", "bes", "c", "des", "ees", "f", "g"});
        keyToWesternNote.put("Ees", new String[]{"ees", "f", "g", "aes", "bes", "c", "d"});
        keyToWesternNote.put("Bes", new String[]{"bes", "c", "d", "ees", "f", "g", "a"});
        keyToWesternNote.put("F", new String[]{"f", "g", "a", "bes", "c", "d", "e"});
        keyToWesternNote.put("C", new String[]{"c", "d", "e", "f", "g", "a", "b"});
        keyToWesternNote.put("G", new String[]{"g", "a", "b", "c", "d", "e", "fis"});
        keyToWesternNote.put("D", new String[]{"d", "e", "fis", "g", "a", "b", "cis"});
        keyToWesternNote.put("A", new String[]{"a", "b", "cis", "d", "e", "fis", "gis"});
        keyToWesternNote.put("E", new String[]{"e", "fis", "gis", "a", "b", "cis", "dis"});
        keyToWesternNote.put("B", new String[]{"b", "cis", "dis", "e", "fis", "gis", "ais"});
        keyToWesternNote.put("Fis", new String[]{"fis", "gis", "a", "b", "cis", "dis", "eis"});
    }

    private static final Map<String, int[]> keyToPitch = new HashMap<>();

    static {
        keyToPitch.put("Ges", new int[]{0, 0, 0, 1, 1, 1, 1});
        keyToPitch.put("Des", new int[]{0, 0, 0, 0, 0, 0, 1});
        keyToPitch.put("Aes", new int[]{0, 0, 1, 1, 1, 1, 1});
        keyToPitch.put("Ees", new int[]{0, 0, 0, 0, 0, 1, 1});
        keyToPitch.put("Bes", new int[]{0, 1, 1, 1, 1, 1, 1});
        keyToPitch.put("F", new int[]{0, 0, 0, 0, 1, 1, 1});
        keyToPitch.put("C", new int[]{0, 0, 0, 0, 0, 0, 0});
        keyToPitch.put("G", new int[]{0, 0, 0, 1, 1, 1, 1});
        keyToPitch.put("D", new int[]{0, 0, 0, 0, 0, 0, 1});
        keyToPitch.put("A", new int[]{0, 0, 1, 1, 1, 1, 1});
        keyToPitch.put("E", new int[]{0, 0, 0, 0, 0, 1, 1});
        keyToPitch.put("B", new int[]{0, 1, 1, 1, 1, 1, 1});
        keyToPitch.put("Fis", new int[]{0, 0, 0, 0, 1, 1, 1});
    }

    private static final String tsfValues = "drmfslt";

    public static WesternNote tsfToWesternNote(TsfNote tsfNote, String key, boolean female) {
        if (tsfNote.isBreak()) {
            return new WesternNote("", 0, tsfNote.getLength(), tsfNote.getAccent(), tsfNote.getType());
        } else {
            return new WesternNote(getNote(tsfNote.getNote(), key), getPitch(tsfNote.getOctave(), key, tsfNote.getNote(), female),
                    tsfNote.getLength(), tsfNote.getAccent(), tsfNote.getType());
        }
    }

    static String getNote(String tsfNote, String key) {
        StringBuilder note = new StringBuilder();
        String[] westernNotes = keyToWesternNote.get(key);
        note.append(westernNotes[tsfValues.indexOf(tsfNote.charAt(0))]);
        if (tsfNote.length() == 2) {
            char c = tsfNote.charAt(1);
            note.append(c == 'a' ? "es" : "is");
        }
        return note.toString();
    }

    static int getPitch(int octave, String key, String note, boolean female) {
        int pitch = keyToPitch.get(key)[tsfValues.indexOf(note)];
        if (female) {
            return pitch + 4 + octave;
        } else {
            return pitch + 3 + octave;
        }
    }
}
