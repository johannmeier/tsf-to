package de.jm.tsfto.parser;

import de.jm.tsfto.model.song.ScorePart;
import de.jm.tsfto.model.song.SongLine;
import de.jm.tsfto.model.song.SongModel;

public class Latex {

    public static String toLatex(SongModel songModel) {
        StringBuilder latexBuilder = new StringBuilder();
        for (Object object : songModel.getSongLines()) {
            if (object instanceof ScorePart) {
                for (SongLine songLine : ((ScorePart) object).getSongLines()) {
                    latexBuilder.append(songLine.toLatex()).append('\n');
                }
            }  else if (object instanceof SongLine) {
                latexBuilder.append(((SongLine) object).toLatex()).append('\n');
            } else {
                throw new RuntimeException("Unknown SongModel object: " + object);
            }
        }
        return latexBuilder.toString();
    }
}
