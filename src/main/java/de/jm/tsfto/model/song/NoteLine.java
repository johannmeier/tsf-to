package de.jm.tsfto.model.song;

public class NoteLine extends SongLine {

    public NoteLine(String line) {
        super(line);
    }

    @Override
    public String toLatex() {
        return "TODO";
    }

    public static boolean matches(String line) {
        final String validChars = "drmfsltaeib_=-+*0123456789!|;:.,/'? ";
        for (char c : line.toCharArray()) {
            if (validChars.indexOf(c) < 0) {
                return false;
            }
        }

        int colonCount = getCount(':', line);
        int bangCount = getCount('!', line);
        return colonCount > 0 && (colonCount + bangCount > 1);
    }

    public static int getCount(char ch, String line) {
        if (line == null) {
            return 0;
        }

        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }
}
