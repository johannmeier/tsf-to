package de.jm.tsfto;

import de.jm.tsfto.cli.Cli;
import de.jm.tsfto.cli.CliApp;
import de.jm.tsfto.cli.CliLogger;
import de.jm.tsfto.cli.annotations.Argument;
import de.jm.tsfto.model.song.SongModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class TsfToApplication extends CliApp {

    private static final Logger logger = CliLogger.getLogger(TsfToApplication.class.getName());

    @Argument(index = 0, mandatory = true)
    private String filename;

    public static void main(String[] args) {
        Cli.run(TsfToApplication.class, args);
    }

    @Override
    public int cliMain(List<String> args) {
        SongModel songModel = SongModel.parse(filename);
        File file = new File(filename);
        String texFilename =  file.getName().replace(".tsf", ".tex");
        try {
            Files.writeString(Path.of(texFilename), songModel.toLatex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
