# Prompt: Music Score PDF — Instrument Page Extraction

You are given a multi-page PDF of a wind band music score. Your task is to analyze each page and produce a CSV that maps page ranges to instruments.

## Instructions

1. Examine every page of the PDF.
2. Identify instrument labels at the top of each page (e.g. "Klarinette 1", "Trompete in Bb", "Altsaxophon 2. Stimme").
3. Group consecutive pages that belong to the same instrument part.
4. For each group, produce exactly one CSV row.
5. Write all rows into a file named `noten.csv`, encoded as **UTF-8**.

## Output

Write the result as a `.csv` file named `noten.csv`.
The file must contain **only** the CSV content — no explanations, no markdown, no BOM, no trailing newline after the last row.

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
One or more instrument names from the list below, joined by `&` if multiple instruments share the exact same pages.
Use the **exact** string from this list (case-insensitive is accepted, but prefer the canonical casing):

| Canonical name      |
|---------------------|
| Piccolo             |
| Flöte               |
| Oboe                |
| Klarinette          |
| Altklarinette       |
| Bassklarinette      |
| Fagott              |
| Sopransaxophon      |
| Altsaxophon         |
| Tenorsaxophon       |
| Baritonsaxophon     |
| Trompete            |
| Cornet              |
| Flügelhorn          |
| Horn                |
| Tenorhorn           |
| Euphonium           |
| Bariton             |
| Posaune             |
| Bass Posaune        |
| Tuba                |
| Bass                |
| Kontrabass          |
| E-Bass              |
| E-Guitar            |
| Piano/Keyboard      |
| Perkussion          |
| Partitur            |
| Vocals              |

If an instrument printed on the score does not match any name above, choose the closest match.

### Column 2 — Stimmen (optional)
The voice/part number, if printed on the score (e.g. "1. Stimme", "2nd part", "Stimme 3").
Use one of: `1`, `2`, `3`, `4`
Join multiple voices with `&` (e.g. `1&2`).
Leave blank if not applicable.

### Column 3 — Seiten (required)
Page range in the format `FROM-TO` (e.g. `3-5`) or a single page number (e.g. `7`).
Use the **physical PDF page numbers** (1-based).

### Column 4 — Stimmlage (optional)
The transposition of the instrument, if printed or clearly implied.
Use one of: `Bb`, `C`, `Eb`, `F`
Leave blank if unknown or not printed.

Common mappings for guidance (not exhaustive):
- Klarinette, Trompete, Cornet, Tenorsaxophon → `Bb`
- Altsaxophon, Baritonsaxophon, Altklarinette → `Eb`
- Horn → typically `F`, sometimes `Eb`
- Flöte, Oboe, Fagott, Posaune, Tuba, Bass, Partitur → `C`

### Column 5 — Notenschlüssel (optional)
Use one of: `Violinschlüssel`, `Bassschlüssel`
Leave blank if not clearly distinguishable.

## Rules

- Every instrument part that appears in the PDF must have exactly one row.
- If two instruments are printed on the same set of pages (e.g. a combined "Flöte / Piccolo" part), list both in column 1 separated by `&`.
- Do **not** merge different instruments into one row unless they literally share the same physical pages.
- Do **not** add rows for instruments not present in the PDF.
- Always include all five fields per row — use empty string (nothing between delimiters) for optional fields that are not applicable.
- Do **not** include any text outside the CSV content in the file.

### Special rule: Perkussion

All percussion pages must be combined into **exactly one row**, regardless of how many distinct percussion parts (e.g. "Schlagzeug 1", "Schlagzeug 2", "Pauken") appear in the PDF.

- Column 1: `Perkussion`
- Column 2: leave blank (do not number the voices)
- Column 3: use the range from the **first** percussion page to the **last** percussion page (e.g. if percussion appears on pages 33–34 and 37–39, write `33-39`)
- Columns 4–5: leave blank

## Example Output

```
Instrumente;Stimmen;Seiten;Stimmlage;Notenschlüssel
Partitur;;1-8;C;Violinschlüssel
Flöte & Piccolo;;9-10;C;Violinschlüssel
Klarinette;1;11-12;Bb;Violinschlüssel
Klarinette;2;13-14;Bb;Violinschlüssel
Altsaxophon;1;15-16;Eb;Violinschlüssel
Altsaxophon;2;17-18;Eb;Violinschlüssel
Tenorsaxophon;;19-20;Bb;Violinschlüssel
Trompete;1;21-22;Bb;Violinschlüssel
Trompete;2;23-24;Bb;Violinschlüssel
Horn;1&2;25-26;F;Violinschlüssel
Posaune;1;27-28;C;Bassschlüssel
Posaune;2;29-30;C;Bassschlüssel
Tuba;;31-32;C;Bassschlüssel
Perkussion;;33-39;;
```
