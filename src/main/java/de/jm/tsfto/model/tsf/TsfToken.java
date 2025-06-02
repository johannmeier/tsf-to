package de.jm.tsfto.model.tsf;

public class TsfToken {
    private final String token;

    public TsfToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return token;
    }
}
