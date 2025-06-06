package de.jm.tsfto.model.tsf;

public class TsfToken {
    private final String token;

    private TsfToken(String token) {
        this.token = token;
    }

    public static TsfToken of(String token) {
        return new TsfToken(token);
    }

    @Override
    public String toString() {
        return token;
    }
}
