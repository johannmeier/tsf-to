TSF = $(notdir $(wildcard tsf/*.tsf))

PDF = $(TSF:%.tsf=%.pdf)

.PRECIOUS: %.tex

all: $(PDF)

clean:
	rm *.log *.aux *.fdb_latexmk *.fls *.tex *.pdf

%.tex: tsf/%.tsf
	echo "tsf -> tex"
	java -cp target/classes de.jm.tsfto.TsfToApplication $< $@


%.pdf: %.tex
	echo $(PDF)
	latexmk $<
