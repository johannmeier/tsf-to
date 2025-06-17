package de.jm.tsfto.model.song;

import org.junit.jupiter.api.Test;

import java.util.List;

class SongModelTest {


    @Test
    void parse() {
        List<String> lines = List.of(
                "T: Alleluja",
                "C: Gordon Young",
                "",
                "K: D-Dur",
                "sql: 9.6",
                "latex: \\footnotesize",
                "",
                "s: Key_D",
                "s: p*6                                                    mp",
                "!l, .- ,l,  :l, .l, :l, .l,  !t, .- ,t, :t, .t, :t, .t,  !d  .- ,d  :d  .d  :d  .d   !r  .- ,r  :r  .r  :r  .r",
                "!l, .- ,l,  :l, .l, :l, .l,  !l, .- ,l, :l, .l, :l, .l,  !l, .- ,l, :l, .l, :l, .l,  !t, .- ,t, :t, .t, :t, .t,",
                "!Al-    *le- lu- ja, al- le- !lu-  *ja,  al- le- lu- ja! !Al-   *le- lu- ja, al- le- !lu-   *ja, al- le- lu- ja!",
                "!           :       :        !          :       :        !          :       :l  .l   !l  .- ,l  :l  .l  :l  .l",
                "!           :       :        !          :       :        !          :       :        !          :       :"
        );

        SongModel songModel = SongModel.parse(lines);
        songModel.getSongLines().forEach(System.out::println);
    }

    @Test
    void parseFile() {
        SongModel songModel = SongModel.parse("tsf/HaveANiceDay.tsf");
        songModel.getSongLines().forEach(System.out::println);
    }

    @Test
    void toLatex() {
        SongModel songModel = SongModel.parse("tsf/HaveANiceDay.tsf");
        System.out.println(songModel.toLatex());
    }
}
