# Prompt: Music Score PDF — Instrument Page Extraction

You are given a multi-page PDF of a wind band music score. Your task is to analyze each page and produce a CSV that maps page ranges to instruments.

## Instructions

1. Examine **every** page of the PDF. Note the instrument label printed at the top of each page, the clef of the first system, and the bar number the page starts at.
2. Identify instrument labels at the top of each page (e.g. "Klarinette 1", "Trompete in Bb", "Altsaxophon 2. Stimme", "1st Bb trumpet", "C bassoon").
3. Decide for every page whether it **continues** the previous page or **starts a new part** (see "Multi-page parts vs. duplicate copies" below).
4. For each part, produce exactly one CSV row.
5. If the same instrument + voice + transposition + clef combination (same columns 1, 2, 4 and 5) appears more than once, keep only the **first** occurrence and skip all subsequent copies.
6. Write all rows into a file named `noten.csv`, encoded as **UTF-8**.

## Output

Write the result as a `.csv` file named `noten.csv`. The file must contain **only** the CSV content — no explanations, no markdown, no BOM, no trailing newline after the last row.

- **Encoding**: UTF-8 (no BOM)
- **Delimiter**: semicolon (`;`)
- **Line endings**: LF (`\n`)
- **First row**: the header line below, verbatim — do not translate or modify it
- **Subsequent rows**: one row per instrument part, five semicolon-separated fields each

Header (copy exactly):

```
Instrumente;Stimmen;Seiten;Stimmlage;Notenschlüssel
```

### Column 1 — Instrumente (required)

One or more instrument names from the list below, joined by `&` if multiple **different** instruments share the exact same pages. Use the **exact** string from this list (case-insensitive is accepted,
but prefer the canonical casing):

| Canonical name  |
|-----------------|
| Piccolo         |
| Flöte           |
| Oboe            |
| Klarinette      |
| Altklarinette   |
| Bassklarinette  |
| Fagott          |
| Sopransaxophon  |
| Altsaxophon     |
| Tenorsaxophon   |
| Baritonsaxophon |
| Trompete        |
| Cornet          |
| Flügelhorn      |
| Horn            |
| Tenorhorn       |
| Euphonium       |
| Bariton         |
| Posaune         |
| Bass Posaune    |
| Tuba            |
| Bass            |
| Kontrabass      |
| E-Bass          |
| E-Guitar        |
| Piano/Keyboard  |
| Perkussion      |
| Partitur        |
| Vocals          |

If an instrument printed on the score does not match any name above, choose the closest match.

**Labels are not always German.** Map foreign-language labels to the canonical name, for example:

- flute → Flöte · oboe → Oboe · bassoon → Fagott · clarinet → Klarinette
- alto clarinet → Altklarinette · bass clarinet → Bassklarinette
- soprano/alto/tenor/baritone saxophone → Sopran-/Alt-/Tenor-/Baritonsaxophon
- trumpet → Trompete · cornet → Cornet · flugelhorn → Flügelhorn · horn → Horn
- baritone → Bariton · trombone → Posaune · bass trombone → Bass Posaune
- euphonium, tenor tuba → Euphonium · tuba → Tuba · bass, basse → Bass
- string bass, contrabass, Kontrabass → Kontrabass
- side drum, tambour, snare, cymbals, bass drum, timpani, Pauken, Schlagzeug, xylophone, Xylophon, glockenspiel, percussion → Perkussion

### Column 2 — Stimmen (optional)

The voice/part number, if printed on the score (e.g. "1. Stimme", "2nd part", "Stimme 3"). Use one of: `1`, `2`, `3`, `4`
Join multiple voices with `&` (e.g. `1&2`). Leave blank if not applicable.

- A printed range means all voices in it: "2nd – 3rd" → `2&3`, "Horn 1, 2" → `1&2`.
- `Solo` and `repiano` count as voice `1` (e.g. "Solo Bb cornet – repiano" → `1`).
- If no number is printed at all, leave the field blank — do not invent one.

### Column 3 — Seiten (required)

Page range in the format `FROM-TO` (e.g. `3-5`) or a single page number (e.g. `7`). Use the **physical PDF page numbers** (1-based).

**The range covers only the pages of that one part — never the pages of its duplicate copies.**
A part printed on a single page yields a single page number, even when several identical copies of it follow immediately after. Only a part whose music genuinely runs across several pages yields a
range.

### Column 4 — Stimmlage (optional)

The transposition of the instrument, if printed or clearly implied. Use one of: `Bb`, `C`, `Eb`, `F`
Leave blank if unknown or not printed.

Common mappings for guidance (not exhaustive):

- Klarinette, Trompete, Cornet, Tenorsaxophon → `Bb`
- Altsaxophon, Baritonsaxophon, Altklarinette → `Eb`
- Horn → typically `F`, sometimes `Eb`
- Flöte, Oboe, Fagott, Posaune, Tuba, Bass, Partitur → `C`

### Column 5 — Notenschlüssel (optional)

Use one of: `Violinschlüssel`, `Bassschlüssel`
Determine it from the clef actually printed at the start of the first system, not from the instrument's usual clef. Leave blank only if genuinely not distinguishable.

## Rules

### Multi-page parts vs. duplicate copies

Consecutive pages carrying the **same instrument label** are only one multi-page part if the music continues. Check this before grouping:

- **Continuation** (→ one row, page range): the following page picks up where the previous one stopped — its first bar number continues the count, it does not repeat the title/composer heading, and it
  usually has no instrument label of its own or shows it as a running header.
- **Duplicate copy** (→ keep the first page only, skip the rest): the following page starts at bar 1 again, repeats the full title heading, and is visually identical to the previous page. Scores are
  often bound with several playing copies of each part.

When in doubt, compare the first bar number and the presence of the title heading; a repeated title heading is the strongest signal for a duplicate copy.

### Deduplication

- The dedup key is the combination of **Instrumente + Stimmen + Stimmlage + Notenschlüssel**
  (columns 1, 2, 4 and 5). Every unique combination of these four fields must appear in the CSV **exactly once**.
- If the same combination occurs multiple times in the PDF (duplicate copies of the very same part), use the pages of the **first** occurrence and ignore all later copies.
- Rows that share instrument and voice but differ in Stimmlage or Notenschlüssel are **different parts, not duplicates** — keep both. A typical case is the same part printed once in `Bb`/
  `Violinschlüssel` and once in `C`/`Bassschlüssel` (e.g. Bariton, Tenorhorn, Posaune, Euphonium), and sometimes a third time in `Bb`/`Bassschlüssel`.
- Treat a blank Stimmlage or Notenschlüssel as a value of its own: a blank does **not** match a filled-in value. Therefore determine columns 4 and 5 consistently for identical parts, so that genuine
  duplicate copies are still recognised as duplicates.

### Combined labels

- If two **different** instruments are printed on the same set of pages (e.g. "Flöte / Piccolo",
  "1st Bb trumpet / Solo Bb cornet"), list both in column 1 separated by `&`.
- If a label gives two **names for the same instrument** (e.g. "Bb euphonium / Bb tenor tuba",
  "Solo Bb clarinet / 1st Bb clarinet", "Bb bass / Bb tuba"), that is **one** instrument — use a single canonical name and do **not** join with `&`.
- Do **not** merge different instruments into one row unless they literally share the same physical pages.
- Do **not** add rows for instruments not present in the PDF.
- Always include all five fields per row — use empty string (nothing between delimiters) for optional fields that are not applicable.
- Do **not** include any text outside the CSV content in the file.

### Special rule: Perkussion

All percussion pages must be combined into **exactly one row**, regardless of how many distinct percussion parts (e.g. "Schlagzeug 1", "Schlagzeug 2", "Pauken", "Side drum", "Cymbals") appear in the
PDF.

- Column 1: `Perkussion`
- Column 2: leave blank (do not number the voices)
- Column 3: the range from the **first** percussion page to the **last** percussion page, **counting only pages that are not duplicate copies**. So if percussion parts appear on pages 33–34 and 37–39,
  write `33-39`; but if pages 80, 81 and 82 hold two percussion parts of which page 82 is a duplicate copy of page 81, write `80-81`.
- Columns 4–5: leave blank

## Example Output

```
Instrumente;Stimmen;Seiten;Stimmlage;Notenschlüssel
Partitur;;1-8;C;Violinschlüssel
Flöte & Piccolo;;9-10;C;Violinschlüssel
Klarinette;1;11-12;Bb;Violinschlüssel
Klarinette;2&3;13-14;Bb;Violinschlüssel
Altsaxophon;1;15-16;Eb;Violinschlüssel
Altsaxophon;2;17-18;Eb;Violinschlüssel
Tenorsaxophon;;19-20;Bb;Violinschlüssel
Trompete & Cornet;1;21-22;Bb;Violinschlüssel
Trompete & Cornet;2&3;23-24;Bb;Violinschlüssel
Horn;1&2;25-26;F;Violinschlüssel
Posaune;1;27-28;C;Bassschlüssel
Posaune;2;29-30;C;Bassschlüssel
Bariton;;31-32;C;Bassschlüssel
Bariton;;33-34;Bb;Violinschlüssel
Tuba;;35-36;C;Bassschlüssel
Perkussion;;37-43;;
```

Note the two `Bariton` rows: same instrument, same (blank) voice, but a different Stimmlage and Notenschlüssel — these are two separate parts and both are kept.

## Final check before writing the file

Verify each point, and correct the rows if a check fails:

1. Every page of the PDF is accounted for: it is either covered by a row, or identified as a duplicate copy, or noted as blank/cover.
2. No `Seiten` range spans a duplicate copy. If two consecutive pages both show the full title heading and both start at bar 1, they must not be in the same range.
3. The four-field dedup key is unique across all rows.
4. Exactly one `Perkussion` row.
5. Encoding UTF-8 without BOM, `;` as delimiter, LF line endings, no trailing newline.
