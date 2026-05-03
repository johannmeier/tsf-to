package de.jm.tsfto.musicxml;

import de.jm.tsfto.model.song.KeyValueLine;
import de.jm.tsfto.model.song.NoteLine;
import de.jm.tsfto.model.song.ScorePart;
import de.jm.tsfto.model.song.SongModel;
import de.jm.tsfto.model.tsf.TsfNote;
import de.jm.tsfto.model.tsf.TsfNote.Accent;
import de.jm.tsfto.parser.TsfTokenParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Converts a TSF song to MusicXML partwise (one &lt;part&gt; per voice).
 *
 * <p>Beat/duration model (DIVISIONS = 12 per quarter note):
 * <pre>
 *   Quarter (12)
 *   ├── first eighth  (pos  0–5): no length prefix
 *   │   ├── first 16th  (pos 0, dur 3): no extra prefix
 *   │   └── second 16th (pos 3, dur ?): , prefix
 *   └── second eighth (pos  6–11): . prefix
 *       ├── first 16th  (pos 6, dur 3): no extra prefix
 *       └── second 16th (pos 9, dur ?): , prefix (after .)
 *
 *   Triplet eighths: / prefix → positions 0, 4, 8 (dur 4 each)
 * </pre>
 *
 * <p>Time signature: number of beat separators (!, |, :, ;) per measure.
 * Women's voices (S, A) → base octave 4; men's (T, B) → base octave 3.
 */
public class MusicXmlWriter {

    /** MusicXML divisions per quarter note. 12 supports 8ths, 16ths, triplets. */
    private static final int BEAT = 12;

    private static final int TREBLE_BASE_OCTAVE = 4;
    private static final int BASS_BASE_OCTAVE   = 3;

    /** Letter names indexed 0=C … 6=B (scientific pitch notation). */
    private static final String[] LETTER_NAMES = {"C", "D", "E", "F", "G", "A", "B"};

    /**
     * Tonic-Solfa degree table: each entry is {scale-degree (0=do…6=ti), alter-adjustment}.
     * The alter-adjustment is applied on top of the key-signature alter for that degree.
     */
    private static final Map<String, int[]> TSF_DEGREE = new LinkedHashMap<>();
    static {
        TSF_DEGREE.put("da", new int[]{0, -1}); // lowered do
        TSF_DEGREE.put("d",  new int[]{0,  0});
        TSF_DEGREE.put("di", new int[]{0,  1}); // raised do
        TSF_DEGREE.put("ra", new int[]{1, -1}); // lowered re
        TSF_DEGREE.put("r",  new int[]{1,  0});
        TSF_DEGREE.put("ri", new int[]{1,  1}); // raised re
        TSF_DEGREE.put("ma", new int[]{2, -1}); // lowered mi (minor 3rd)
        TSF_DEGREE.put("m",  new int[]{2,  0});
        TSF_DEGREE.put("mi", new int[]{2,  1}); // raised mi
        TSF_DEGREE.put("fa", new int[]{3, -1}); // lowered fa
        TSF_DEGREE.put("f",  new int[]{3,  0});
        TSF_DEGREE.put("fi", new int[]{3,  1}); // raised fa (tritone)
        TSF_DEGREE.put("sa", new int[]{4, -1}); // lowered sol (tritone)
        TSF_DEGREE.put("s",  new int[]{4,  0});
        TSF_DEGREE.put("si", new int[]{4,  1}); // raised sol
        TSF_DEGREE.put("la", new int[]{5, -1}); // lowered la (minor 6th)
        TSF_DEGREE.put("l",  new int[]{5,  0});
        TSF_DEGREE.put("li", new int[]{5,  1}); // raised la
        TSF_DEGREE.put("ta", new int[]{6, -1}); // lowered ti (minor 7th)
        TSF_DEGREE.put("ba", new int[]{6, -1}); // lowered ti (alternate)
        TSF_DEGREE.put("t",  new int[]{6,  0});
        TSF_DEGREE.put("ti", new int[]{6,  1}); // raised ti
    }

    /**
     * Tonic letter index (0=C … 6=B) for key fifths −7…+7.
     * Array index = fifths + 7.
     * Pattern follows the circle of fifths: C G D A E B F# C# / F Bb Eb Ab Db Gb Cb.
     */
    private static final int[] TONIC_LETTER = {0, 4, 1, 5, 2, 6, 3, 0, 4, 1, 5, 2, 6, 3, 0};

    /** Order in which sharps are added (F C G D A E B → letter indices 3 0 4 1 5 2 6). */
    private static final int[] SHARP_ORDER = {3, 0, 4, 1, 5, 2, 6};
    /** Order in which flats are added (B E A D G C F → letter indices 6 2 5 1 4 0 3). */
    private static final int[] FLAT_ORDER  = {6, 2, 5, 1, 4, 0, 3};

    private record Pitch(String step, int alter, int octave) {}

    /** duration = MusicXML divisions; triplet = needs time-modification.
     *  secondPitch is non-null for split-voice (%) notes. */
    private record NoteEntry(TsfNote note, Pitch pitch, Pitch secondPitch,
                              boolean tieStart, boolean tieStop,
                              int duration, boolean triplet) {}

    private record BeatGroupsResult(List<List<TsfNote>> groups, List<String> terminators) {}
    private record MeasureData(List<List<NoteEntry>> measures, List<String> barlineTypes) {}
    private record VoiceData(String name, int partId, List<List<NoteEntry>> measures, List<String> measureBarlines) {}

    // --- public API ---------------------------------------------------------

    public void convert(Path tsfFile, Path xmlFile) throws IOException {
        Files.writeString(xmlFile, toMusicXml(tsfFile));
    }

    public String toMusicXml(Path tsfFile) {
        return toMusicXml(SongModel.parse(tsfFile.toString()));
    }

    public String toMusicXml(SongModel songModel) {
        Map<String, String> meta   = extractMetadata(songModel);
        int keyFifths              = getKeyFifths(meta.get("K"));
        List<VoiceData>     voices = collectVoices(songModel, keyFifths);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE score-partwise PUBLIC \"-//Recordare//DTD MusicXML 3.1 Partwise//EN\" ");
        sb.append("\"http://www.musicxml.org/dtds/partwise.dtd\">\n");
        sb.append("<score-partwise version=\"3.1\">\n");

        appendHeader(sb, meta);
        appendPartList(sb, voices);

        int beatsPerMeasure = detectBeatsPerMeasure(songModel);
        int bpm = parseBpm(meta.get("bpm"));
        boolean firstVoice = true;
        for (VoiceData voice : voices) {
            appendVoicePart(sb, voice, keyFifths, beatsPerMeasure, firstVoice ? bpm : 0);
            firstVoice = false;
        }

        sb.append("</score-partwise>\n");
        return sb.toString();
    }

    // --- voice collection ---------------------------------------------------

    private List<VoiceData> collectVoices(SongModel songModel, int keyFifths) {
        Map<String, List<TsfNote>> voiceNotes = new LinkedHashMap<>();
        List<String> lastVoiceOrder = new ArrayList<>();

        for (Object obj : songModel.getSongLines()) {
            if (!(obj instanceof ScorePart scorePart)) continue;
            List<NoteLine> voicedLines = getNoteLinesOf(scorePart);
            if (!voicedLines.isEmpty()) {
                lastVoiceOrder = voicedLines.stream().map(NoteLine::getVoice).toList();
                for (NoteLine nl : voicedLines)
                    voiceNotes.computeIfAbsent(nl.getVoice(), k -> new ArrayList<>()).addAll(nl.getTsfNotes());
            } else {
                List<NoteLine> allLines = getAllNoteLinesOf(scorePart);
                for (int i = 0; i < Math.min(allLines.size(), lastVoiceOrder.size()); i++)
                    voiceNotes.computeIfAbsent(lastVoiceOrder.get(i), k -> new ArrayList<>())
                              .addAll(allLines.get(i).getTsfNotes());
            }
        }

        int partId = 1;
        List<VoiceData> result = new ArrayList<>();
        for (Map.Entry<String, List<TsfNote>> e : voiceNotes.entrySet()) {
            String voice      = e.getKey();
            int    baseOctave = baseOctaveForVoice(voice);
            MeasureData md = buildMeasures(e.getValue(), baseOctave, keyFifths);
            result.add(new VoiceData(voice, partId++, md.measures(), md.barlineTypes()));
        }

        // Pad voices that start mid-piece (fewer measures) with whole-measure rests at the front.
        int maxMeasures = result.stream().mapToInt(v -> v.measures().size()).max().orElse(0);
        List<List<NoteEntry>> refMeasures = result.stream()
                .max(Comparator.comparingInt(v -> v.measures().size()))
                .map(VoiceData::measures).orElse(List.of());
        List<VoiceData> aligned = new ArrayList<>();
        for (VoiceData vd : result) {
            int pad = maxMeasures - vd.measures().size();
            if (pad <= 0) { aligned.add(vd); continue; }
            List<List<NoteEntry>> paddedMeasures = new ArrayList<>();
            List<String> paddedBarlines = new ArrayList<>();
            for (int i = 0; i < pad; i++) {
                int dur = i < refMeasures.size()
                        ? refMeasures.get(i).stream().mapToInt(NoteEntry::duration).sum()
                        : BEAT * 4;
                paddedMeasures.add(restMeasure(dur));
                paddedBarlines.add(null);
            }
            paddedMeasures.addAll(vd.measures());
            paddedBarlines.addAll(vd.measureBarlines());
            aligned.add(new VoiceData(vd.name(), vd.partId(), paddedMeasures, paddedBarlines));
        }
        return aligned;
    }

    private List<NoteEntry> restMeasure(int duration) {
        TsfNote rest = new TsfNote(0, "", TsfNote.Length.UNKNOWN, Accent.NONE, ":", "");
        return List.of(new NoteEntry(rest, null, null, false, false, duration, false));
    }

    private List<NoteLine> getAllNoteLinesOf(ScorePart scorePart) {
        return scorePart.getSongLines().stream()
                .filter(l -> l instanceof NoteLine nl && NoteLine.matches(nl.getLine()))
                .map(l -> (NoteLine) l)
                .toList();
    }

    private List<NoteLine> getNoteLinesOf(ScorePart scorePart) {
        return scorePart.getSongLines().stream()
                .filter(l -> l instanceof NoteLine nl
                        && NoteLine.matches(nl.getLine())
                        && !nl.getVoice().isEmpty())
                .map(l -> (NoteLine) l)
                .toList();
    }

    // --- time signature detection -------------------------------------------

    private int detectBeatsPerMeasure(SongModel songModel) {
        for (Object obj : songModel.getSongLines()) {
            if (!(obj instanceof ScorePart sp)) continue;
            for (NoteLine nl : getNoteLinesOf(sp)) {
                int beats = beatsInFirstMeasure(nl.getTsfNotes());
                if (beats > 0) return beats;
            }
        }
        return 4;
    }

    private int beatsInFirstMeasure(List<TsfNote> notes) {
        int beats = 0;
        boolean seenBar = false;
        for (TsfNote note : notes) {
            if (note.isEndOfPart()) continue;
            Accent a = note.getAccent();
            if (a == Accent.BAR || a == Accent.DOUBLE_BAR) {
                if (!seenBar) { seenBar = true; beats = 1; }
                else          { break; }                     // second bar = end
            } else if (seenBar && (a == Accent.NONE || a == Accent.ACCENTED)) {
                beats++;
            }
        }
        return beats;
    }

    // --- measure / beat-group building --------------------------------------

    private MeasureData buildMeasures(List<TsfNote> notes, int baseOctave, int keyFifths) {
        BeatGroupsResult split = splitIntoBeatGroups(notes);
        List<List<TsfNote>> beatGroups  = split.groups();
        List<String>        terminators = split.terminators();

        int[][] diatonicScale = getDiatonicScale(keyFifths);
        int     tonicLetter   = TONIC_LETTER[keyFifths + 7];
        Pitch   lastPitch     = resolvePitch("d", 0, diatonicScale, tonicLetter, baseOctave);

        // Phase 1: flat list of NoteEntries (ties not yet set) + measure-boundary + per-note barline
        List<NoteEntry> flat         = new ArrayList<>();
        List<Boolean>   startsNew    = new ArrayList<>(); // true = first note of new measure
        List<String>    flatBarlines = new ArrayList<>(); // barline from the group this note belongs to
        boolean         firstGroup   = true;

        for (int gi = 0; gi < beatGroups.size(); gi++) {
            List<TsfNote> bg = beatGroups.get(gi);
            boolean newMeasure = isMeasureStart(bg.get(0).getAccent()) && !firstGroup;
            List<int[]> durInfo = computeDurations(bg);

            for (int i = 0; i < bg.size(); i++) {
                TsfNote note = bg.get(i);
                Pitch pitch = lastPitch;
                if (note.isNote()) {
                    pitch     = resolvePitch(note.getNote(), note.getOctave(),
                                             diatonicScale, tonicLetter, baseOctave);
                    lastPitch = pitch;
                }
                Pitch secondPitch = null;
                if (note.isStack()) {
                    String s = note.getSecondNote();
                    if (!s.isEmpty()) {
                        TsfNote sn = TsfTokenParser.getPlainNote(s);
                        secondPitch = resolvePitch(sn.getNote(), sn.getOctave(),
                                                   diatonicScale, tonicLetter, baseOctave);
                    }
                }
                flat.add(new NoteEntry(note, pitch, secondPitch, false, false,
                                       durInfo.get(i)[0], durInfo.get(i)[1] == 1));
                startsNew.add(i == 0 && newMeasure);
                flatBarlines.add(terminators.get(gi));
            }
            firstGroup = false;
        }

        // Phase 2: set ties by scanning the full flat list
        for (int i = 0; i < flat.size(); i++) {
            NoteEntry e = flat.get(i);
            boolean tieStop  = e.note().isContinue();
            boolean tieStart = (i + 1 < flat.size()) && flat.get(i + 1).note().isContinue();
            flat.set(i, new NoteEntry(e.note(), e.pitch(), e.secondPitch(),
                                      tieStart, tieStop, e.duration(), e.triplet()));
        }

        // Phase 2.5: merge note + following continues into a single note when the combined
        // duration is a standard value and no bar line lies in between.
        // The merged note inherits the barline of the last absorbed continue (propagating
        // section-end barlines that live on trailing :-  notes through the merge).
        // Continue notes (including !-) can serve as merge heads so that a full-measure
        // tie continuation like "!- :- :- :-" collapses to a single whole note.
        for (int i = 0; i < flat.size(); i++) {
            NoteEntry e = flat.get(i);
            if (!e.tieStart()) continue;
            while (i + 1 < flat.size()
                    && flat.get(i + 1).note().isContinue()
                    && !startsNew.get(i + 1)) {
                int combined = e.duration() + flat.get(i + 1).duration();
                if (!isValidDuration(combined)) break;
                String inherited = flatBarlines.get(i + 1) != null
                        ? flatBarlines.get(i + 1) : flatBarlines.get(i);
                e = new NoteEntry(e.note(), e.pitch(), e.secondPitch(),
                                  flat.get(i + 1).tieStart(), e.tieStop(),
                                  combined, e.triplet());
                flat.set(i, e);
                flat.remove(i + 1);
                startsNew.remove(i + 1);
                flatBarlines.set(i, inherited);
                flatBarlines.remove(i + 1);
            }
        }

        // Phase 3: split flat list into measures using boundary flags
        List<List<NoteEntry>> measures        = new ArrayList<>();
        List<String>          measureBarlines = new ArrayList<>();
        List<NoteEntry>       measure         = new ArrayList<>();
        String                lastBarline     = null;

        for (int i = 0; i < flat.size(); i++) {
            if (startsNew.get(i) && !measure.isEmpty()) {
                measures.add(measure);
                measureBarlines.add(lastBarline);
                measure = new ArrayList<>();
            }
            measure.add(flat.get(i));
            lastBarline = flatBarlines.get(i);
        }
        if (!measure.isEmpty()) {
            measures.add(measure);
            measureBarlines.add(lastBarline);
        }
        return new MeasureData(measures, measureBarlines);
    }

    private static BeatGroupsResult splitIntoBeatGroups(List<TsfNote> notes) {
        List<List<TsfNote>> beatGroups  = new ArrayList<>();
        List<String>        terminators = new ArrayList<>();
        List<TsfNote>       group       = new ArrayList<>();

        for (TsfNote note : notes) {
            if (note.isEndOfPart()) {
                if (!group.isEmpty()) {
                    beatGroups.add(group);
                    terminators.add("light-light");
                    group = new ArrayList<>();
                }
                continue;
            }
            if (isBeatStart(note.getAccent()) && !group.isEmpty()) {
                beatGroups.add(group);
                terminators.add(note.getAccent() == Accent.DOUBLE_BAR ? "light-light" : null);
                group = new ArrayList<>();
            }
            group.add(note);
        }
        if (!group.isEmpty()) {
            beatGroups.add(group);
            terminators.add(null);
        }

        return new BeatGroupsResult(beatGroups, terminators);
    }

    /**
     * Assigns durations to notes within one beat group using the positional model.
     *
     * <pre>
     *   prefix (length part)  position
     *   (none / accent only)     0
     *   .                        6   (second eighth)
     *   , (before .)             3   (second 16th of first eighth)
     *   , (after  .)             9   (second 16th of second eighth)
     *   / (1st)                  0   (triplet)
     *   / (2nd)                  4
     *   / (3rd)                  8
     * </pre>
     *
     * Duration of note i = position[i+1] − position[i]  (last → BEAT − position[last]).
     *
     * @return list of [duration, isTriplet(0/1)] parallel to the input list
     */
    private List<int[]> computeDurations(List<TsfNote> group) {
        int n = group.size();
        int[] positions  = new int[n];
        int[] isTriplet  = new int[n];

        boolean seenDot    = false;
        int     tripletIdx = 0;

        for (int i = 0; i < n; i++) {
            char lc = lengthChar(group.get(i).getPrefix());
            switch (lc) {
                case '.' -> { positions[i] = 6; seenDot = true; }
                case ',' -> positions[i] = seenDot ? 9 : 3;
                case '/' -> { positions[i] = tripletIdx * 4; tripletIdx++; isTriplet[i] = 1; }
                default  -> positions[i] = 0;
            }
        }

        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int nextPos = (i + 1 < n) ? positions[i + 1] : BEAT;
            int dur     = nextPos - positions[i];
            if (dur <= 0) dur = BEAT; // safety fallback
            result.add(new int[]{dur, isTriplet[i]});
        }
        return result;
    }

    /** Returns the first length-relevant character (.  ,  /) from a prefix, or 0. */
    private static char lengthChar(String prefix) {
        for (char c : prefix.toCharArray()) {
            if (c == '.' || c == ',' || c == '/') return c;
        }
        return 0;
    }

    private static boolean isBeatStart(Accent a) {
        return a == Accent.BAR || a == Accent.DOUBLE_BAR
                || a == Accent.NONE || a == Accent.ACCENTED;
    }

    private static boolean isMeasureStart(Accent a) {
        return a == Accent.BAR || a == Accent.DOUBLE_BAR;
    }

    // --- metadata -----------------------------------------------------------

    private Map<String, String> extractMetadata(SongModel songModel) {
        Map<String, String> meta = new HashMap<>();
        for (Object obj : songModel.getSongLines()) {
            if (obj instanceof KeyValueLine kv) meta.put(kv.getKey(), kv.getValue());
        }
        return meta;
    }

    private void appendHeader(StringBuilder sb, Map<String, String> meta) {
        String title    = stripLatex(meta.getOrDefault("T", ""));
        String composer = stripLatex(meta.getOrDefault("C", ""));
        if (!title.isEmpty()) {
            sb.append("  <work>\n");
            sb.append("    <work-title>").append(escapeXml(title)).append("</work-title>\n");
            sb.append("  </work>\n");
        }
        if (!composer.isEmpty()) {
            sb.append("  <identification>\n");
            sb.append("    <creator type=\"composer\">").append(escapeXml(composer)).append("</creator>\n");
            sb.append("  </identification>\n");
        }
    }

    private void appendPartList(StringBuilder sb, List<VoiceData> voices) {
        sb.append("  <part-list>\n");
        for (VoiceData v : voices) {
            String pid = "P" + v.partId();
            sb.append("    <score-part id=\"").append(pid).append("\">\n");
            sb.append("      <part-name>").append(escapeXml(voiceDisplayName(v.name()))).append("</part-name>\n");
            sb.append("      <score-instrument id=\"").append(pid).append("-I1\">\n");
            sb.append("        <instrument-name>Grand Piano</instrument-name>\n");
            sb.append("      </score-instrument>\n");
            sb.append("      <midi-instrument id=\"").append(pid).append("-I1\">\n");
            sb.append("        <midi-channel>").append(v.partId()).append("</midi-channel>\n");
            sb.append("        <midi-program>1</midi-program>\n");
            sb.append("      </midi-instrument>\n");
            sb.append("    </score-part>\n");
        }
        sb.append("  </part-list>\n");
    }

    // --- parts --------------------------------------------------------------

    private void appendVoicePart(StringBuilder sb, VoiceData voice,
                                  int keyFifths, int beatsPerMeasure, int bpm) {
        sb.append("  <part id=\"P").append(voice.partId()).append("\">\n");

        int     measureNumber = 1;
        boolean firstMeasure  = true;

        int totalMeasures = voice.measures().size();
        for (int mi = 0; mi < totalMeasures; mi++) {
            List<NoteEntry> measure     = voice.measures().get(mi);
            String          barlineType = voice.measureBarlines().get(mi);
            boolean         isLast      = (mi == totalMeasures - 1);
            if (isLast) barlineType = "light-heavy";
            sb.append("    <measure number=\"").append(measureNumber++).append("\">\n");
            if (firstMeasure) {
                appendAttributes(sb, keyFifths, voice.name(), beatsPerMeasure);
                if (bpm > 0) appendTempo(sb, bpm);
                firstMeasure = false;
            }
            for (NoteEntry entry : measure) appendNote(sb, entry);
            if (barlineType != null) appendBarline(sb, barlineType);
            sb.append("    </measure>\n");
        }

        sb.append("  </part>\n");
    }

    private void appendBarline(StringBuilder sb, String barStyle) {
        sb.append("      <barline location=\"right\">\n");
        sb.append("        <bar-style>").append(barStyle).append("</bar-style>\n");
        sb.append("      </barline>\n");
    }

    private void appendTempo(StringBuilder sb, int bpm) {
        sb.append("      <direction placement=\"above\">\n");
        sb.append("        <direction-type>\n");
        sb.append("          <metronome parentheses=\"no\">\n");
        sb.append("            <beat-unit>quarter</beat-unit>\n");
        sb.append("            <per-minute>").append(bpm).append("</per-minute>\n");
        sb.append("          </metronome>\n");
        sb.append("        </direction-type>\n");
        sb.append("        <sound tempo=\"").append(bpm).append("\"/>\n");
        sb.append("      </direction>\n");
    }

    // --- XML emission -------------------------------------------------------

    private void appendAttributes(StringBuilder sb, int keyFifths,
                                   String voice, int beatsPerMeasure) {
        sb.append("      <attributes>\n");
        sb.append("        <divisions>").append(BEAT).append("</divisions>\n");
        sb.append("        <key><fifths>").append(keyFifths).append("</fifths></key>\n");
        sb.append("        <time><beats>").append(beatsPerMeasure)
          .append("</beats><beat-type>4</beat-type></time>\n");
        appendClef(sb, voice);
        sb.append("      </attributes>\n");
    }

    private void appendClef(StringBuilder sb, String voice) {
        String v = voice == null ? "" : voice.toLowerCase();
        if (v.startsWith("b")) {
            sb.append("        <clef><sign>F</sign><line>4</line></clef>\n");
        } else if (v.startsWith("t")) {
            sb.append("        <clef><sign>G</sign><line>2</line>");
            sb.append("<clef-octave-change>-1</clef-octave-change></clef>\n");
        } else {
            sb.append("        <clef><sign>G</sign><line>2</line></clef>\n");
        }
    }

    private void appendNote(StringBuilder sb, NoteEntry entry) {
        TsfNote note = entry.note();
        sb.append("      <note>\n");

        if (note.isBreak()) {
            sb.append("        <rest/>\n");
        } else {
            Pitch p = entry.pitch();
            sb.append("        <pitch>\n");
            sb.append("          <step>").append(p.step()).append("</step>\n");
            if (p.alter() != 0)
                sb.append("          <alter>").append(p.alter()).append("</alter>\n");
            sb.append("          <octave>").append(p.octave()).append("</octave>\n");
            sb.append("        </pitch>\n");
        }

        sb.append("        <duration>").append(entry.duration()).append("</duration>\n");

        if (!note.isBreak()) {
            if (entry.tieStop())  sb.append("        <tie type=\"stop\"/>\n");
            if (entry.tieStart()) sb.append("        <tie type=\"start\"/>\n");
        }

        sb.append("        <voice>1</voice>\n");
        sb.append("        <type>").append(noteType(entry.duration(), entry.triplet())).append("</type>\n");

        // Dotted note: duration is not a power-of-two multiple of BEAT → add <dot/>
        if (isDotted(entry.duration())) {
            sb.append("        <dot/>\n");
        }

        if (entry.triplet()) {
            sb.append("        <time-modification>");
            sb.append("<actual-notes>3</actual-notes>");
            sb.append("<normal-notes>2</normal-notes>");
            sb.append("</time-modification>\n");
        }

        boolean hasTie          = !note.isBreak() && (entry.tieStop() || entry.tieStart());
        boolean hasArticulation = !note.isBreak() && (note.isAccented() || note.isMarcato()
                                                   || note.isStaccato() || note.isTenuto());
        if (hasTie || entry.triplet() || hasArticulation) {
            sb.append("        <notations>\n");
            if (hasTie) {
                if (entry.tieStop())  sb.append("          <tied type=\"stop\"/>\n");
                if (entry.tieStart()) sb.append("          <tied type=\"start\"/>\n");
            }
            if (entry.triplet()) {
                String tupletType = entry.tieStop() ? "stop" : "start";
                sb.append("          <tuplet type=\"").append(tupletType).append("\"/>\n");
            }
            if (hasArticulation) {
                sb.append("          <articulations>\n");
                if (note.isAccented())       sb.append("            <accent/>\n");
                if (note.isMarcato())        sb.append("            <strong-accent/>\n");
                if (note.isPortato())        sb.append("            <detached-legato/>\n");
                else if (note.isStaccato())  sb.append("            <staccato/>\n");
                else if (note.isTenuto())    sb.append("            <tenuto/>\n");
                sb.append("          </articulations>\n");
            }
            sb.append("        </notations>\n");
        }

        sb.append("      </note>\n");

        if (entry.secondPitch() != null) {
            Pitch sp = entry.secondPitch();
            sb.append("      <note>\n");
            sb.append("        <chord/>\n");
            sb.append("        <pitch>\n");
            sb.append("          <step>").append(sp.step()).append("</step>\n");
            if (sp.alter() != 0)
                sb.append("          <alter>").append(sp.alter()).append("</alter>\n");
            sb.append("          <octave>").append(sp.octave()).append("</octave>\n");
            sb.append("        </pitch>\n");
            sb.append("        <duration>").append(entry.duration()).append("</duration>\n");
            if (entry.tieStop())  sb.append("        <tie type=\"stop\"/>\n");
            if (entry.tieStart()) sb.append("        <tie type=\"start\"/>\n");
            sb.append("        <voice>1</voice>\n");
            sb.append("        <type>").append(noteType(entry.duration(), entry.triplet())).append("</type>\n");
            if (isDotted(entry.duration())) sb.append("        <dot/>\n");
            if (entry.tieStop() || entry.tieStart()) {
                sb.append("        <notations>\n");
                if (entry.tieStop())  sb.append("          <tied type=\"stop\"/>\n");
                if (entry.tieStart()) sb.append("          <tied type=\"start\"/>\n");
                sb.append("        </notations>\n");
            }
            sb.append("      </note>\n");
        }
    }

    // --- helpers ------------------------------------------------------------

    // --- pitch resolution (moveable-do) -------------------------------------

    /** Alter value (+1 sharp / -1 flat / 0) for each letter C…B in the given key. */
    private static int[] getKeyAlters(int keyFifths) {
        int[] alters = new int[7];
        if (keyFifths > 0) {
            for (int i = 0; i < Math.min(keyFifths, 7); i++) alters[SHARP_ORDER[i]] = 1;
        } else {
            for (int i = 0; i < Math.min(-keyFifths, 7); i++) alters[FLAT_ORDER[i]]  = -1;
        }
        return alters;
    }

    /**
     * Returns the 7 diatonic scale degrees for the given key as {letterIndex, alter} pairs.
     * Index 0 = do (tonic), 1 = re, …, 6 = ti.
     */
    private static int[][] getDiatonicScale(int keyFifths) {
        int[] alters = getKeyAlters(keyFifths);
        int   tonic  = TONIC_LETTER[keyFifths + 7];
        int[][] scale = new int[7][2];
        for (int i = 0; i < 7; i++) {
            int letterIdx = (tonic + i) % 7;
            scale[i][0] = letterIdx;
            scale[i][1] = alters[letterIdx];
        }
        return scale;
    }

    /**
     * Resolves a Tonic-Solfa syllable + octave modifier to an absolute MusicXML pitch.
     *
     * <p>Because scientific-pitch octave numbers reset at C, notes whose letter index
     * is lower than the tonic's letter (e.g. t=C# in D-major) need +1 to their octave.
     */
    private static Pitch resolvePitch(String noteName, int octaveMod,
                                      int[][] diatonicScale, int tonicLetter,
                                      int baseOctave) {
        int[] degreeInfo = TSF_DEGREE.get(noteName);
        if (degreeInfo == null) return new Pitch("C", 0, baseOctave + octaveMod);

        int degree      = degreeInfo[0];
        int alterAdjust = degreeInfo[1];

        int letterIdx = diatonicScale[degree][0];
        int alter     = diatonicScale[degree][1] + alterAdjust;

        // Notes that "wrap around" past C land in the next scientific octave relative to the tonic
        int octaveShift = (letterIdx < tonicLetter) ? 1 : 0;

        return new Pitch(LETTER_NAMES[letterIdx], alter, baseOctave + octaveMod + octaveShift);
    }

    /** Returns the MusicXML note type string for the given duration. */
    private String noteType(int duration, boolean triplet) {
        if (triplet) return "eighth";  // triplet-eighth (dur=4)
        return switch (duration) {
            case 48 -> "whole";
            case 36 -> "half";    // dotted half
            case 24 -> "half";
            case 18 -> "quarter"; // dotted quarter
            case 12 -> "quarter";
            case  9 -> "eighth";  // dotted eighth
            case  6 -> "eighth";
            case  3 -> "16th";
            default -> "quarter";
        };
    }

    /** Returns true when the duration is a dotted note value (9, 18, or 36). */
    private boolean isDotted(int duration) {
        return duration == 9 || duration == 18 || duration == 36;
    }

    /** Returns true when the duration can be expressed as a single MusicXML note. */
    private boolean isValidDuration(int duration) {
        return switch (duration) {
            case 3, 4, 6, 9, 12, 18, 24, 36, 48 -> true;
            default -> false;
        };
    }

    private int baseOctaveForVoice(String voice) {
        if (voice == null) return TREBLE_BASE_OCTAVE;
        String v = voice.toLowerCase();
        return (v.startsWith("t") || v.startsWith("b")) ? BASS_BASE_OCTAVE : TREBLE_BASE_OCTAVE;
    }

    private String voiceDisplayName(String voice) {
        if (voice == null || voice.isEmpty()) return "Voice";
        return switch (voice.toLowerCase()) {
            case "s"  -> "Soprano";
            case "s2" -> "Soprano II";
            case "a"  -> "Alto";
            case "a2" -> "Alto II";
            case "t"  -> "Tenor";
            case "t2" -> "Tenor II";
            case "b"  -> "Bass";
            case "b2" -> "Bass II";
            default   -> voice;
        };
    }

    /** Strips key qualifiers like "D-Dur", "Bb-Moll" → "D", "Bb". */
    private int getKeyFifths(String key) {
        if (key == null) return 0;
        String root = key.trim().split("[-\\s]")[0];
        return switch (root) {
            case "G"  ->  1; case "D"  ->  2; case "A"  ->  3;
            case "E"  ->  4; case "B"  ->  5; case "F#" ->  6; case "C#" ->  7;
            case "F"  -> -1; case "Bb" -> -2; case "Eb" -> -3;
            case "Ab" -> -4; case "Db" -> -5; case "Gb" -> -6; case "Cb" -> -7;
            default   ->  0;
        };
    }

    private int parseBpm(String bpmStr) {
        if (bpmStr == null) return 0;
        try { return Integer.parseInt(bpmStr.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;")
                   .replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Strips LaTeX markup from a text string, keeping plain content. */
    private String stripLatex(String text) {
        if (text == null) return "";
        // \\ is a LaTeX newline — replace with a separator
        text = text.replace("\\\\", " – ");
        // {\cmd content} → content  (e.g. {\small Subtitle})
        text = text.replaceAll("\\{\\\\[a-zA-Z]+\\s+([^}]*)\\}", "$1");
        // \cmd{content} → content
        text = text.replaceAll("\\\\[a-zA-Z]+\\{([^}]*)\\}", "$1");
        // remaining standalone \cmd → remove
        text = text.replaceAll("\\\\[a-zA-Z]+", "");
        // clean up stray braces
        text = text.replace("{", "").replace("}", "");
        return text.trim();
    }
}
