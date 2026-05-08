# RNA2DUnifier — Complete User Guide

> **Version:** 0.0.1  
> **Java:** 21  
> **Build system:** Maven  
> **Group ID:** `it.unicam.cs.bdslab`  
> **Artifact ID:** `rna2d-unifier`

---

## Table of Contents

1. [Overview](#1-overview)
2. [Background — Formats and Nomenclature](#2-background--formats-and-nomenclature)
3. [Architecture](#3-architecture)
4. [Getting Started](#4-getting-started)
   - [Prerequisites](#41-prerequisites)
   - [Build](#42-build)
   - [Add as Dependency](#43-add-as-dependency)
5. [Quick Start](#5-quick-start)
6. [Core API](#6-core-api)
   - [RnaUnifier](#61-rnaunifier)
   - [ToolType](#62-tooltype)
   - [ParserFactory](#63-parserfactory)
   - [RnaStructureParser](#64-rnastructureparser)
   - [ExtendedRNASecondaryStructure](#65-extendedrnaasecondarystructure)
   - [Pair](#66-pair)
   - [BondType](#67-bondtype)
   - [BpseqExporter](#68-bpseqexporter)
7. [Supported Input Formats](#7-supported-input-formats)
8. [Output Formats](#8-output-formats)
9. [Auto-Detection of Tool Type](#9-auto-detection-of-tool-type)
10. [Logging](#10-logging)
11. [Error Handling](#11-error-handling)
12. [Running Tests](#12-running-tests)
13. [Advanced Usage](#13-advanced-usage)
14. [Troubleshooting](#14-troubleshooting)

---

## 1. Overview

**RNA2DUnifier** is a Java library that reads the output of several RNA secondary structure analysis tools and converts it into a single, unified format called **extended BPSEQ**. This allows downstream tools and pipelines to work with a consistent representation regardless of which tool produced the original annotation.

The library currently supports seven tools:

| Tool | Format type |
|------|-------------|
| FR3D | JSON |
| RNAview | Plain text |
| RNApolis | FASTA + tabular |
| mc-annotate | Plain text |
| Barnaba | Tabular annotations |
| bpnet (BPFIND) | Tabular annotations |
| x3dna-DSSR | JSON |

All interactions are classified using the **Leontis–Westhof** geometric nomenclature and exported to an extended BPSEQ file where each nucleotide position is associated with its pairing partners for every interaction family.

---

## 2. Background — Formats and Nomenclature

### 2.1 BPSEQ

The standard BPSEQ format is a three-column tab-separated text file:

```
Index   Nucleotide   PairingPartner
1       G            29
2       G            28
3       G            27
...
```

- **Index** is 1-based.
- **PairingPartner** is `0` for unpaired positions, or the 1-based index of the paired nucleotide.

### 2.2 Extended BPSEQ

RNA2DUnifier introduces an **extended BPSEQ** format that adds one column for each of the 12 Leontis–Westhof interaction families. Each extra column lists the 1-based index (or indices) of pairing partners for that specific interaction type at that position. `0` means no partner of that type.

Example header:

```
Index   Nucleotide   cWW   tWW   cWH   tWH   cWS   tWS   cHH   tHH   cHS   tHS   cSS   tSS
```

### 2.3 Leontis–Westhof Nomenclature

The 12 geometric families describe base pairs in terms of the interacting edges (Watson–Crick **W**, Hoogsteen **H**, Sugar **S**) and the glycosidic bond orientation (cis **c** / trans **t**):

| Code | Edges | Orientation |
|------|-------|-------------|
| cWW  | W / W | cis |
| tWW  | W / W | trans |
| cWH  | W / H | cis |
| tWH  | W / H | trans |
| cWS  | W / S | cis |
| tWS  | W / S | trans |
| cHH  | H / H | cis |
| tHH  | H / H | trans |
| cHS  | H / S | cis |
| tHS  | H / S | trans |
| cSS  | S / S | cis |
| tSS  | S / S | trans |

Canonical Watson–Crick base pairs (A–U, G–C) are classified as **cWW**. The **tWW** type is also considered canonical by the library.

---

## 3. Architecture

```
RNA2DUnifier
│
├── RnaUnifier                  ← Main entry point (facade)
│
├── parser/
│   ├── RnaStructureParser      ← Interface
│   ├── ToolType                ← Enum of supported tools
│   ├── ParserFactory           ← Instantiates and auto-detects parsers
│   └── impl/
│       ├── BarnabaParser
│       ├── BpnetParser
│       ├── Fr3dParser
│       ├── McAnnotateParser
│       ├── RnapolisParser
│       ├── RnaviewParser
│       └── X3dnaParser
│
├── listeners/                  ← ANTLR tree-walk listeners (one per tool)
│   ├── barnaba/
│   ├── bpnet/
│   ├── fr3d/
│   ├── mcAnnotate/
│   ├── RNApolis/
│   ├── RNAview/
│   └── x3dna/
│
├── models/
│   ├── ExtendedRNASecondaryStructure  ← Main domain model
│   ├── Pair                           ← A single base pair
│   └── BondType                       ← Leontis–Westhof enum
│
└── exporter/
    └── BpseqExporter           ← Converts model → BPSEQ / extended BPSEQ string
```

Each parser uses an ANTLR-generated lexer/parser driven by a grammar (`.g4`) defined in `src/main/antlr4/`. The grammar walks are handled by custom **listeners** that populate `ExtendedRNASecondaryStructure` through its **Builder**.

---

## 4. Getting Started

### 4.1 Prerequisites

- **Java 21** (LTS) or later
- **Maven 3.8+**

### 4.2 Build

Clone the repository and build with Maven:

```bash
git clone <repository-url>
cd RNA2DUnifier
mvn clean install
```

To skip tests during the build:

```bash
mvn clean install -DskipTests
```

The compiled JAR will be placed in `target/rna2d-unifier-0.0.1.jar`.

### 4.3 Add as Dependency

After installing locally, add the following to your project's `pom.xml`:

```xml
<dependency>
    <groupId>it.unicam.cs.bdslab</groupId>
    <artifactId>rna2d-unifier</artifactId>
    <version>0.0.1</version>
</dependency>
```

---

## 5. Quick Start

### Parse a file with explicit tool type

```java
import it.unicam.cs.bdslab.rna2dunifier.RnaUnifier;
import it.unicam.cs.bdslab.rna2dunifier.parser.ToolType;

import java.io.File;

public class Example {
    public static void main(String[] args) throws Exception {
        RnaUnifier unifier = new RnaUnifier();

        File inputFile = new File("path/to/barnaba_output.out");

        // true  → extended BPSEQ (all 12 LW families)
        // false → canonical BPSEQ only (cWW + tWW)
        String result = unifier.process(inputFile, ToolType.BARNABA, true);

        System.out.println(result);
    }
}
```

### Parse a file with auto-detection

```java
RnaUnifier unifier = new RnaUnifier();
File inputFile = new File("path/to/rnaview_output.txt");

// Tool type is inferred automatically from file content
String result = unifier.process(inputFile, true);
System.out.println(result);
```

### Write the output directly to a file

```java
RnaUnifier unifier = new RnaUnifier();
File inputFile  = new File("path/to/fr3d_output.json");
File outputFile = new File("path/to/output.bpseq");

unifier.processToFile(inputFile, ToolType.FR3D, outputFile, true);
```

---

## 6. Core API

### 6.1 `RnaUnifier`

`RnaUnifier` is the main facade class. Instantiate it with the default constructor or inject a custom `BpseqExporter`.

```java
// Default usage
RnaUnifier unifier = new RnaUnifier();

// With custom exporter
RnaUnifier unifier = new RnaUnifier(myCustomExporter);
```

#### Methods

| Method | Description |
|--------|-------------|
| `String process(File, ToolType, boolean)` | Parse `File` using the specified tool parser. `boolean` selects extended (`true`) or canonical (`false`) BPSEQ. |
| `String process(File, boolean)` | Same as above but auto-detects the tool type from the file content. |
| `String process(InputStream, ToolType, boolean)` | Parse from an `InputStream` with explicit tool type. |
| `String process(InputStream, boolean)` | Parse from a mark/reset-capable `InputStream` with auto-detection. |
| `void processToFile(File, ToolType, File, boolean)` | Parse and write the result directly to an output file. |
| `void processToFile(File, File, boolean)` | Same, with auto-detection of tool type. |

> **Note on `boolean extended`:**  
> Pass `true` to obtain the full extended BPSEQ (all 12 Leontis–Westhof columns).  
> Pass `false` to obtain canonical BPSEQ only (Watson–Crick pairs, positions with no canonical partner are omitted).

---

### 6.2 `ToolType`

An enum listing every supported tool. Import it from `it.unicam.cs.bdslab.rna2dunifier.parser.ToolType`.

| Constant | Tool / Format |
|----------|---------------|
| `FR3D` | FR3D JSON output |
| `RNAVIEW` | RNAview plain-text output |
| `RNAPOLIS` | RNApolis FASTA + tabular output |
| `MCANNOTATE` | mc-annotate plain-text output |
| `BARNABA` | Barnaba tabular annotation output |
| `BPNET` | bpnet (BPFIND) tabular output |
| `X3DNA` | x3dna-DSSR JSON output |

---

### 6.3 `ParserFactory`

A static factory with two responsibilities: creating parser instances and auto-detecting the tool type.

```java
// Get a parser manually
RnaStructureParser parser = ParserFactory.getParser(ToolType.RNAPOLIS);

// Detect the tool from a stream (stream must support mark/reset)
ToolType detected = ParserFactory.detectTool(bufferedInputStream);
```

#### Detection heuristics

Auto-detection reads the first 4 096 bytes of the stream and looks for format-specific signatures:

| Priority | Signal | Detected tool |
|----------|--------|---------------|
| 1 | Contains `BEGIN_base-pair` | `RNAVIEW` |
| 2 | Contains `Residue conformations` | `MCANNOTATE` |
| 3 | Contains `>` **and** `seq ` | `RNAPOLIS` |
| 4 | JSON with key `"annotations"` | `FR3D` |
| 5 | JSON with key `"pairs"` | `X3DNA` |
| 6 | Lines matching `N_INT_INT N_INT_INT XXc` pattern | `BARNABA` |
| 7 | Lines with `?` separator and `W:WC`-style tokens | `BPNET` |

If no signature matches, an `IllegalArgumentException` is thrown.

> **Important:** the stream passed to `detectTool` must support `mark()`/`reset()`. Wrap a plain `FileInputStream` in a `BufferedInputStream` before calling this method.

---

### 6.4 `RnaStructureParser`

The interface implemented by all seven concrete parsers.

```java
public interface RnaStructureParser {
    ExtendedRNASecondaryStructure parse(InputStream inputStream)
            throws IOException, ParseException;
}
```

You do not normally need to use the concrete parsers directly — `ParserFactory.getParser(ToolType)` is the preferred way to obtain one.

---

### 6.5 `ExtendedRNASecondaryStructure`

The central domain model. Build instances through the nested **Builder**.

#### Builder usage

```java
ExtendedRNASecondaryStructure structure =
    new ExtendedRNASecondaryStructure.Builder()
        .setSequence("AUGCAUGC")
        .addPair(new Pair(0, 7, "A", "U", BondType.LEONTIS_WESTHOF_cWW))
        .addPair(new Pair(1, 6, "U", "G", BondType.LEONTIS_WESTHOF_tWW))
        .addHeaderInfo("PDB ID", "1YMO")
        .addHeaderInfo("Chain ID", "A")
        .build();
```

#### Key methods

| Method | Returns | Description |
|--------|---------|-------------|
| `getSequence()` | `String` | The RNA nucleotide sequence (A, C, G, U, N). |
| `getPairs()` | `List<Pair>` | All base pairs including non-canonical and stacking. |
| `getCanonical()` | `List<Pair>` | Only canonical pairs (cWW or tWW). |
| `getHeaderInfo()` | `Map<String,String>` | Metadata such as PDB ID, Chain ID (unmodifiable). |

> **Builder note:** `addPair(Pair)` automatically adds the pair to both the full list and — if `pair.getType().isCanonical()` is true — to the canonical list. Use `setPairs()` / `setCanonical()` only when you want to replace the lists wholesale.

---

### 6.6 `Pair`

Represents a single base-pair interaction. Instances are immutable. Two `Pair` objects are equal if they share the same unordered position pair and bond type (i.e., swapping pos1/pos2 still yields the same pair).

#### Constructors

```java
// Minimal: positions + bond type
Pair pair = new Pair(0, 29, BondType.LEONTIS_WESTHOF_cWW);

// With nucleotide labels
Pair pair = new Pair(0, 29, "G", "C", BondType.LEONTIS_WESTHOF_cWW);

// Via Builder
Pair pair = new Pair.Builder()
    .setPos1(0)
    .setPos2(29)
    .setNucleotide1("G")
    .setNucleotide2("C")
    .setType(BondType.LEONTIS_WESTHOF_cWW)
    .build();
```

#### Key methods

| Method | Returns | Description |
|--------|---------|-------------|
| `getPos1()` | `int` | Zero-based index of the first nucleotide. |
| `getPos2()` | `int` | Zero-based index of the second nucleotide. |
| `getType()` | `BondType` | The Leontis–Westhof bond type. |
| `getNucleotide1()` | `String` | Nucleotide label at pos1 (may be `null`). |
| `getNucleotide2()` | `String` | Nucleotide label at pos2 (may be `null`). |

> **Positions are zero-based internally.** The exporter converts them to 1-based indices in the BPSEQ output.

---

### 6.7 `BondType`

An enum covering all Leontis–Westhof families plus special types.

```java
// Get a BondType from a string label (case-insensitive)
BondType type = BondType.fromString("cWW");   // → LEONTIS_WESTHOF_cWW
BondType type = BondType.fromString("tHS");   // → LEONTIS_WESTHOF_tHS
BondType type = BondType.fromString(null);    // → UNKNOWN

// Query the type
type.isCanonical();   // true for cWW and tWW
type.isCis();         // true for all cXX types
type.isTrans();       // true for all tXX types
type.getInfo();       // e.g. "cWW", "tHS", "stacking", "unknown"

// Retrieve all 12 LW types in a consistent order
List<BondType> lwFamilies = BondType.getLeontisWesthofFamily();
```

Special values:

| Constant | Meaning |
|----------|---------|
| `UNKNOWN` | Unclassified or unrecognised bond |
| `STACKING` | Base-stacking interaction (not a base pair) |

`fromString` normalises the string: it uppercases it and corrects reversed edge notation (e.g., `SH` → `HS`, `SW` → `WS`, `HW` → `WH`) before matching.

---

### 6.8 `BpseqExporter`

Converts an `ExtendedRNASecondaryStructure` into a string.

```java
BpseqExporter exporter = new BpseqExporter();

// Full extended BPSEQ (12 LW columns)
String extended = exporter.printExtendedBPSEQ(structure);

// Canonical BPSEQ only (positions without a canonical partner are omitted)
String canonical = exporter.printCanonicalBPSEQ(structure);
```

**Extended BPSEQ output structure:**

```
Index   Nucleotide   cWW   tWW   cWH   tWH   cWS   tWS   cHH   tHH   cHS   tHS   cSS   tSS
1       G            29    0     0     0     0     0     0     0     0     0     0     0
2       G            28    0     0     0     0     0     0     0     0     0     0     0
...
```

If a nucleotide has multiple partners of the same type (e.g., two cWH interactions), the partners are comma-separated: `3,17`.

**Canonical BPSEQ output structure:**

```
1   G   29
2   G   28
...
```

> Positions with no canonical partner are **not** included in the canonical output.

---

## 7. Supported Input Formats

### 7.1 Barnaba

Barnaba produces two output files per structure: a **pairing** file and a **stacking** file. RNA2DUnifier processes the pairing file.

**Typical content:**

```
# Loading /data/preprocessed/1YMO_A.pdb
# ./barnaba/bin/barnaba ANNOTATE --pdb /data/preprocessed/1YMO_A.pdb
#RES1       RES2       ANNO
# PDB 1YMO_A.pdb
# sequence G_1_0-G_2_0-C_3_0-...
G_1_0      C_29_0      WCc
G_2_0      C_28_0      WCc
U_5_0      A_25_0      SHc
```

Each interaction line contains two residue identifiers (nucleotide + PDB number) and a Leontis–Westhof annotation in Barnaba's compact notation (e.g., `WCc` = Watson–Crick/cis).

Example:
- [1YMO_A.pdb.ANNOTATE.pairing.out](src/test/resources/rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out)
- [1YMO_A.pdb.ANNOTATE.stacking.out](src/test/resources/rna-output/barnaba/1YMO_A.pdb.ANNOTATE.stacking.out)

### 7.2 bpnet

bpnet produces a structured plain-text report. A data line looks like:

```
5       5   U ? A       25    25   A ? A    W:WC BP 0.24    35    35   A ? A    S:HC TP 1.59
```

The `W:WC` token encodes the bond type in BPFIND notation.

Example:
- [1YMO_A.1YMO_A.out](src/test/resources/rna-output/bpnet/1YMO_A.1YMO_A.out)

### 7.3 FR3D

FR3D outputs JSON. RNA2DUnifier looks for the key `"modified"` and `"annotations"` at the top level.

```json
{
  "pdb_id": "1YMO_A",
  "chain_id": "A",
  "modified": [],
  "annotations": [
    {
      "seq_id1": "19",
      "3d_id1": "19",
      "nt1": "A",
      "unit1": "A",
      "bp": "cWW",
      "seq_id2": "42",
      "nt2": "U",
      "unit2": "U",
      "3d_id2": "42",
      "crossing": "0"
    }
  ]
}
```
Example: 
- [1YMO_A_A_basepair.json](src/test/resources/rna-output/fr3d/1YMO_A_A_basepair.json)
### 7.4 mc-annotate

mc-annotate produces a plain-text report with a `Residue conformations` section followed by stacking and pairing sections. A pairing line looks like:

```
A1-A29 : G-C Ww/Ww pairing antiparallel cis XIX
```
Example:
- [1YMO_A.txt](src/test/resources/rna-output/mc-annotate/txt/1YMO_A.txt)
### 7.5 RNApolis

RNApolis output begins with a FASTA-style sequence header (`>...`) and includes a tabular listing of pairs with the keyword `seq`. The parser reconstructs the sequence from the header.
It has dot-bracket notation for base pairs.

```
>strand_A
seq GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAGUCAGCA
cWW [[[[[[........((.((((((]]]]]]........)))))).)).
```

Example: 
- [1YMO_A.3db](src/test/resources/rna-output/rnapolis/1YMO_A.3db)

### 7.6 RNAview

RNAview output contains a `BEGIN_base-pair` block. Interaction lines have the form:

```
1_29, A:     1 G-C    29 A: +/+ cis         XIX
```

The last field is the Saenger Classification (e.g., `XIX` = cWW in Leontis Westhof notation).

Example:
- [1YMO_A.pdb.out](src/test/resources/rna-output/rnaview/1YMO_A.pdb.out)

### 7.7 x3dna-DSSR

x3dna-DSSR produces JSON. RNA2DUnifier identifies this format by the presence of the `"pairs"` key.

```json
{
  "pairs": [
    {
      "index": 1,
      "nt1": "A.G1",
      "nt2": "A.C29",
      "bp": "G-C",
      "name": "WC",
      "Saenger": "19-XIX",
      "LW": "cWW",
      "DSSR": "cW-W"
    }
  ]
}
```
Example:
- [1YMO_A.json](src/test/resources/rna-output/x3dna-dssr/1YMO_A.json)

---

## 8. Output Formats

### 8.1 Extended BPSEQ

The extended BPSEQ is a tab-separated file:

```
Index	Nucleotide	cWW	tWW	cWH	tWH	cWS	tWS	cHH	tHH	cHS	tHS	cSS	tSS
1	G	29	0	0	0	0	0	0	0	0	0	0	0
2	G	28	0	0	0	0	0	0	0	0	0	0	0
5	U	25	0	0	0	0	0	0	0	0	35  0	0
...
```

- Each nucleotide always appears (one row per position in the sequence).
- `0` means no partner of that type.
- Multiple partners for a single type at one position are listed as comma-separated values (e.g., `3,17`).

### 8.2 Canonical BPSEQ

The canonical BPSEQ follows the classic format and contains only positions that participate in at least one canonical pair:

```
1	G	29
2	G	28
4	C	26
...
```

---

## 9. Auto-Detection of Tool Type

When no `ToolType` is provided, `RnaUnifier` delegates to `ParserFactory.detectTool()`. This method peeks at the first 4 096 bytes of the stream.

**Requirements:**

- The `InputStream` must support `mark()`/`reset()`. A `BufferedInputStream` always does.
- If you pass a raw `FileInputStream`, wrap it:

```java
try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile))) {
    String result = unifier.process(bis, true);
}
```

`RnaUnifier.process(File, boolean)` wraps the file internally, so you do not need to do this manually when passing a `File`.

---

## 10. Logging

RNA2DUnifier uses **SLF4J** as its logging façade, backed at runtime by **Logback** (included as a compile-scope dependency). All listener classes emit structured log messages during parsing.

### 10.1 Log Levels Used

| Level | When it appears |
|-------|----------------|
| `TRACE` | Fine-grained mapping of individual residues to zero-based indices. |
| `DEBUG` | Entry/exit of parse sections (residue block, stacking lines, pair lines). |
| `INFO` | Summary messages at the end of parsing (sequence length, total pairs counted). |
| `WARN` | Non-fatal anomalies: malformed tokens, skipped uncommon residues, residue number gaps, unrecognised bond strings, pairs whose residues cannot be resolved. |

### 10.2 Configuring Logback

Place a `logback.xml` (or `logback-test.xml` for tests) on the classpath. Example configuration to route RNA2DUnifier logs to the console at `INFO` level:

```xml
<configuration>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} – %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Set the level for all RNA2DUnifier parsers -->
    <logger name="it.unicam.cs.bdslab.rna2dunifier" level="INFO"/>

    <root level="WARN">
        <appender-ref ref="CONSOLE"/>
    </root>

</configuration>
```

To enable detailed `TRACE` output for a specific parser (e.g., mc-annotate):

```xml
<logger name="it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate" level="TRACE"/>
```

### 10.3 Using a Different Backend

Because RNA2DUnifier depends only on `slf4j-api`, you can replace Logback with any SLF4J-compatible implementation. If your project already uses Log4j 2, add the `log4j-slf4j2-impl` bridge and exclude `logback-classic`:

```xml
<dependency>
    <groupId>it.unicam.cs.bdslab</groupId>
    <artifactId>rna2d-unifier</artifactId>
    <version>0.0.1</version>
    <exclusions>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 10.4 Silencing All Logs

To suppress all RNA2DUnifier output without changing any configuration file, set the logger level programmatically before calling the library:

```java
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

Logger root = (Logger) LoggerFactory.getLogger("it.unicam.cs.bdslab.rna2dunifier");
root.setLevel(Level.OFF);
```

---

## 11. Error Handling

All parsing methods declare `IOException` and `ParseException`. Additionally, `detectTool` and the auto-detecting overloads throw `IllegalArgumentException` when the format cannot be identified.

```java
try {
    String result = unifier.process(inputFile, true);
} catch (IOException e) {
    // File could not be read
    System.err.println("I/O error: " + e.getMessage());
} catch (java.text.ParseException e) {
    // File was readable but content did not match the expected grammar
    System.err.println("Parse error at offset " + e.getErrorOffset() + ": " + e.getMessage());
} catch (IllegalArgumentException e) {
    // Auto-detection failed — the format was not recognised
    System.err.println("Unknown format: " + e.getMessage());
}
```

**Common causes of `ParseException`:**

- The file is in a valid format for tool A but you passed `ToolType.B`.
- The file is truncated or corrupted.
- The tool version produced output with a minor format variation not covered by the grammar.

**Common causes of `IllegalArgumentException` (auto-detection):**

- The file is empty or too small (fewer than a few lines).
- The format is entirely custom or not one of the seven supported tools.

---

## 12. Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=BarnabaParserTest

# Run integration tests only
mvn test -Dtest=ParserIntegrationTest,RnaUnifierIntegrationTest
```

Test resources (sample files for all seven tools) are located in `src/test/resources/rna-output/`.

---

## 13. Advanced Usage

### 13.1 Processing an InputStream Directly

Useful when the input comes from a network connection, a ZIP entry, or any source that is not a plain file:

```java
InputStream rawStream = mySource.openStream();
BufferedInputStream bis = new BufferedInputStream(rawStream);

RnaUnifier unifier = new RnaUnifier();
String result = unifier.process(bis, ToolType.FR3D, true);
```

### 13.2 Building a Structure Programmatically

You can construct an `ExtendedRNASecondaryStructure` by hand without going through a parser — useful for testing or for integrating with other data sources:

```java
ExtendedRNASecondaryStructure structure =
    new ExtendedRNASecondaryStructure.Builder()
        .setSequence("GCAUGC")
        .addPair(new Pair(0, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW))
        .addPair(new Pair(1, 4, "C", "G", BondType.LEONTIS_WESTHOF_cWW))
        .addPair(new Pair(0, 3, "G", "U", BondType.LEONTIS_WESTHOF_tWH))
        .addHeaderInfo("Source", "manual")
        .build();

BpseqExporter exporter = new BpseqExporter();
System.out.println(exporter.printExtendedBPSEQ(structure));
```

### 13.3 Inspecting Pairs After Parsing

After parsing, you can iterate over all pairs and filter by type:

```java
RnaStructureParser parser = ParserFactory.getParser(ToolType.RNAPOLIS);
ExtendedRNASecondaryStructure structure;
try (InputStream is = new FileInputStream("input.txt")) {
    structure = parser.parse(is);
}

// All canonical pairs
structure.getCanonical().forEach(p ->
    System.out.println(p.getPos1()+1 + " → " + (p.getPos2()+1))
);

// All cHS pairs
structure.getPairs().stream()
    .filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cHS)
    .forEach(System.out::println);
```

### 13.4 Custom Exporter

If you need a different output format, implement your own exporter and inject it:

```java
public class MyExporter extends BpseqExporter {
    @Override
    public String printExtendedBPSEQ(ExtendedRNASecondaryStructure s) {
        // your custom logic
    }
}

RnaUnifier unifier = new RnaUnifier(new MyExporter());
```

---

## 14. Troubleshooting

### "Unable to detect the tool type from the provided input"

The auto-detection heuristic did not recognise any known signature in the first 4 096 bytes. Use the explicit `ToolType` overload instead, or verify that the file is not corrupted or empty.

### Sequence contains many 'N' characters

When a parser cannot find an explicit nucleotide label for a position, it inserts `'N'` as a placeholder. This is expected for positions that appear in interaction lines but not in the sequence header. A `WARN` log entry is emitted for each such case.

### `IllegalStateException: mark/reset not supported`

Wrap your `InputStream` in `new BufferedInputStream(...)` before passing it to any auto-detecting method.

### Wrong pairs or missing interactions

- Make sure you passed the correct `ToolType`. Mismatched tool types can cause a grammar mismatch and silently produce incomplete structures or throw `ParseException`.
- Check `WARN`-level log output: skipped pairs are always reported with a reason.

### Numbers don't match expected output

Positions in the `Pair` model are **zero-based**. The BPSEQ output is **1-based** (the exporter adds 1 when printing). If you read positions from `Pair.getPos1()` / `getPos2()` directly, add 1 to compare with BPSEQ output.
