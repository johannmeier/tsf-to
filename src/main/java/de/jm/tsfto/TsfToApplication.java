package de.jm.tsfto;

import de.jm.tsfto.cli.Cli;
import de.jm.tsfto.cli.CliApp;
import de.jm.tsfto.cli.CliLogger;
import de.jm.tsfto.cli.annotations.Argument;
import de.jm.tsfto.cli.annotations.Flag;
import de.jm.tsfto.model.song.SongModel;
import de.jm.tsfto.musicxml.MusicXmlWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class TsfToApplication extends CliApp {

    private static final Logger logger = CliLogger.getLogger(TsfToApplication.class.getName());

    @Argument(index = 0, mandatory = true)
    private String filename;

    @Flag(name = "musicxml", description = "Also write a MusicXML (.xml) file")
    private boolean musicxml;

    public static void main(String[] args) {
        Cli.run(TsfToApplication.class, args);
    }

    @Override
    public int cliMain(List<String> args) {
        SongModel songModel = SongModel.parse(filename);
        File file = new File(filename);
        String baseName = file.getName().replace(".tsf", "");
        try {
            Files.writeString(Path.of(baseName + ".tex"), songModel.toLatex());
            if (musicxml) {
                new MusicXmlWriter().convert(file.toPath(), Path.of(baseName + ".xml"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
