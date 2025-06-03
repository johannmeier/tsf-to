package de.wsc.tsfto.cli.exceptions;

public class CliRuntimeException extends RuntimeException{
    public CliRuntimeException(Throwable t) {
        super(t);
    }

    public CliRuntimeException(String message) {
        super(message);
    }
}
