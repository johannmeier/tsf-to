package de.jm.tsfto.cli;

import java.text.MessageFormat;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CliFormatter extends SimpleFormatter {

    @Override
    public String format(LogRecord logRecord) {
        Object[] parameters = logRecord.getParameters();
        String message;

        if (parameters == null || parameters.length == 0) {
            message = logRecord.getMessage();
        } else {
            message = MessageFormat.format(logRecord.getMessage(), logRecord.getParameters());
        }
        return message + '\n';
    }
}