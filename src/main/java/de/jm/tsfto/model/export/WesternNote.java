package de.jm.tsfto.model.export;

import de.jm.tsfto.model.tsf.TsfNote;

import java.util.*;

public class WesternNote {

    private static final float quarter = 1 / 4f;

    private static final Map<TsfNote.Length, Float> tsfLengthToFloat = new HashMap<>();

    static {
        tsfLengthToFloat.put(TsfNote.Length.FULL, quarter);
        tsfLengthToFloat.put(TsfNote.Length.HALF_QUARTER, quarter + quarter / 2);
        tsfLengthToFloat.put(TsfNote.Length.TWO_THIRDS, quarter / 3 * 2);
        tsfLengthToFloat.put(TsfNote.Length.HALF, quarter / 2);
        tsfLengthToFloat.put(TsfNote.Length.THIRD, quarter / 3);
        tsfLengthToFloat.put(TsfNote.Length.QUARTER, quarter / 4);
        tsfLengthToFloat.put(TsfNote.Length.SIXTH, quarter / 6);
        tsfLengthToFloat.put(TsfNote.Length.EIGHTS, quarter / 8);
    }

    private final String note;
    private final int pitch; //scientific pitch notation

    private final TsfNote.Accent accent;
    private final TsfNote.Type type;

    private final List<TsfNote.Length> lengthInTsf = new ArrayList<>();
    private float lengthInWholes = 0;

    public WesternNote(String note, int pitch, TsfNote.Length lengthInTsf, TsfNote.Accent accent, TsfNote.Type type) {
        this.note = note;
        this.pitch = pitch;
        this.lengthInTsf.add(lengthInTsf);
        this.lengthInWholes += tsfLengthToFloat.get(lengthInTsf);
        this.accent = accent;
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public int getPitch() {
        return pitch;
    }

    public float getLengthInWholes() {
        return lengthInWholes;
    }

    public List<TsfNote.Length> getLengthInTsf() {
        return lengthInTsf;
    }

    public TsfNote.Accent getAccent() {
        return accent;
    }

    public TsfNote.Type getType() {
        return type;
    }

    public void addTsfLength(TsfNote.Length length) {
        lengthInTsf.add(length);
        lengthInWholes += tsfLengthToFloat.get(length);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WesternNote that = (WesternNote) o;
        return pitch == that.pitch && Float.compare(lengthInWholes, that.lengthInWholes) == 0 && Objects.equals(note, that.note)
                && Objects.equals(lengthInTsf, that.lengthInTsf) && accent == that.accent && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, pitch, lengthInWholes, lengthInTsf, accent, type);
    }

    @Override
    public String toString() {
        return "WesternNote{" + "note='" + note + '\'' +
                ", pitch=" + pitch +
                ", accent=" + accent +
                ", type=" + type +
                ", lengthInTsf=" + lengthInTsf +
                '}';
    }
}
