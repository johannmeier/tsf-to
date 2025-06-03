package de.jm.tsfto.cli;

import java.io.PrintStream;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class CliOutputHandler extends StreamHandler {
    private Level maxLevel = Level.INFO;

    public CliOutputHandler(PrintStream stream, Formatter formatter) {
        super(stream, formatter);
    }

    public void setMaxLevel(Level newMaxLevel) {
        maxLevel = newMaxLevel;
    }

    @Override
    public synchronized void publish(LogRecord logRecord) {
        if (logRecord.getLevel().intValue() <= maxLevel.intValue()) {
            super.publish(logRecord);
        }
    }
}
