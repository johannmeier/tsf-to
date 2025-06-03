package de.jm.tsfto.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CliLogger {

    private static final Map<String, Logger> loggers = new HashMap<>();
    private static final ConsoleHandler consoleHandler = new ConsoleHandler();
    private static final CliOutputHandler cliOutputHandler = new CliOutputHandler(System.out, new CliFormatter());

    private CliLogger() {
    }

    public static Logger getLogger(String name) {
        if (!loggers.containsKey(name)) {
            loggers.put(name, initLogger(Logger.getLogger(name)));
        }

        return loggers.get(name);
    }


    private static Logger initLogger(Logger logger) {
        Level level = getLogLevel();

        logger.setLevel(level);
        ConsoleHandler errorHandler = consoleHandler;
        errorHandler.setLevel(Level.WARNING);
        errorHandler.setFormatter(new CliFormatter());

        CliOutputHandler outputHandler = cliOutputHandler;
        outputHandler.setLevel(level);
        outputHandler.setMaxLevel(Level.INFO);

        logger.addHandler(errorHandler);
        logger.addHandler(outputHandler);
        logger.setUseParentHandlers(false);

        return logger;
    }

    private static Level getLogLevel() {
        String logLevel = Cli.getPlainCliConfig().getProperty("loglevel");
        Level level;
        if (logLevel == null) {
            level = Level.INFO;
        } else {
            level = Level.parse(logLevel);
            if (level == null) {
                level = Level.INFO;
            }
        }
        return level;
    }
}
