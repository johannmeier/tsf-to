# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Does

`tsf-to` converts `.tsf` (Tonic-SolFa) music notation files into LaTeX (for PDF sheet music) and optionally MusicXML. It is a Java 21 Maven project. The LaTeX output uses the `tonic-solfa.sty` package in this repo.

## Build & Run Commands

```bash
# Build
mvn clean compile
mvn package          # builds JAR

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=SongModelTest

# Run a single test method
mvn test -Dtest=TsfTokenParserTest#parse

# Run the converter (after compile)
java -cp target/classes de.jm.tsfto.TsfToApplication <input.tsf>
java -cp target/classes de.jm.tsfto.TsfToApplication --musicxml <input.tsf> <output.tex>

# Build PDFs from all TSF sources (requires latexmk)
make
```

## Architecture

**Data flow:** `.tsf` file → `SongModel.parse()` → structured line objects → `Latex.java` or `MusicXmlWriter.java` → output file.

### Parsing Layer (`src/main/java/de/jm/tsfto/parser/`)
- `TsfTokenParser` — tokenizes a single TSF note-line string into `TsfNote` objects. Well-tested.
- `SymbolParser` — parses symbol-line tokens.
- `MergeCols` — merges adjacent columns for formatting.

### Domain Model (`src/main/java/de/jm/tsfto/model/`)
- `SongModel` — top-level orchestrator; parses the whole `.tsf` file into `ScorePart`s, `VersePart`s, and `KeyValueLine`s.
- `SongLine` (abstract) subclasses: `NoteLine`, `TextLine`, `SymbolLine`, `KeyValueLine`, `ColsLine`, `VerseLine`.
- `TsfNote` — value object representing one parsed note with enums for `Length`, `Accent`, `Type`.

### Output Layer
- `Latex.java` (`src/main/java/de/jm/tsfto/latex/`) — converts the parsed model to LaTeX using `tonic-solfa.sty` macros.
- `MusicXmlWriter.java` (`src/main/java/de/jm/tsfto/musicxml/`) — exports to MusicXML.

### CLI (`src/main/java/de/jm/tsfto/cli/`)
Custom annotation-driven CLI framework. `TsfToApplication` wires it up; `CliApp`/`Cli.java` handle argument parsing.

## TSF Format Quick Reference

- Note lines use Tonic-SolFa signs (`:`, `d`, `r`, `m`, `f`, `s`, `l`, `t`). `|` or `!` = bar, `;` = accent, `/` = triplet separator, `` ` `` = eighth separator.
- Symbol lines start with `s:` (or any text line containing `~`).
- Text lines support `*text` (right-align), `text*` (left-align), `!`/`!!` (bar), `_` (space), `>` (filler), verse numbers (`1.`, `2.`).
- Key-value pairs (`key: value`) set song metadata (title, key, bpm, etc.).

## Known Static-State Bugs

Two static fields cause data contamination when processing multiple files in the same JVM:

- `SongModel.keyValuePairs` is `static final` and never cleared between parses.
- `ScorePart.countScorePart` is a static counter that is never reset.

These are safe to ignore for single-file CLI invocations but will cause bugs in any multi-file or multi-threaded usage.

## LaTeX Package Sync

`Latex.java` and `tonic-solfa.sty` must stay in sync. If you add new LaTeX macros in Java, add the corresponding `\newcommand` to `tonic-solfa.sty`. The macros `\akc`, `\nkc`, `\hkc` are referenced in Java but may be missing from the `.sty` file — verify before generating output.
