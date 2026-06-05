# RNA2DUnifier

**RNA2DUnifier** is a Java library for parsing and unifying RNA secondary structure annotation files produced by multiple bioinformatics tools into a single, consistent format.

It is designed for researchers and developers who need a reliable, tool-agnostic representation of RNA base-pair interactions annotated with the full Leontis–Westhof nomenclature.

---

## 📚 Documentation

For detailed documentation please visit the [**RNA2DUnifier Wiki**](https://github.com/bdslab/RNA2DUnifier/wiki).

---

## 🧬 Overview

RNA secondary structure annotation tools (FR3D, RNAview, RNApolis, mc-annotate, Barnaba, bpnet, x3dna-DSSR) each produce different, incompatible output formats.  
**RNA2DUnifier** provides a unified pipeline to:

- Automatically **detect the source tool** from file content
- **Parse** the tool-specific output using ANTLR4-based grammars
- **Convert** annotations into a unified extended BPSEQ format
- Classify all interactions according to the **Leontis–Westhof** geometric nomenclature
- Export either a **canonical** or **extended** BPSEQ representation

The library uses an **ANTLR4-based parser** for each supported tool, ensuring robust and formal parsing of all supported formats.

---

## 🔬 Supported Tools

| Tool | Output Format |
|------|--------------|
| **FR3D** | JSON (`"annotations"` key) |
| **RNAview** | Plain text (`BEGIN_base-pair` block) |
| **RNApolis** | FASTA header + tabular pairs |
| **mc-annotate** | Plain text (`Residue conformations` section) |
| **Barnaba** | Tabular residue–pair annotations |
| **bpnet (BPFIND)** | Tabular pairs with `?` separator |
| **x3dna-DSSR** | JSON (`"pairs"` key) |

---

## ⚙️ Requirements

- Java **21+**
- Maven **3.8+**

---

## 📦 Installation

Build the library locally:

```bash
mvn clean install
```

Add dependency:

```xml
<dependency>
    <groupId>it.unicam.cs.bdslab</groupId>
    <artifactId>rna2d-unifier</artifactId>
    <version>0.0.1</version>
</dependency>
```

---

## 🚀 Basic Usage

### Parse a File with Explicit Tool Type

```java
RnaUnifier unifier = new RnaUnifier();

String result = unifier.process(
    new File("barnaba_output.out"),
    ToolType.BARNABA,
    true   // true = extended BPSEQ, false = canonical only
);

System.out.println(result);
```

---

### Parse with Auto-Detection

```java
RnaUnifier unifier = new RnaUnifier();

// Tool type is inferred automatically from the file content
String result = unifier.process(new File("rnaview_output.txt"), true);
```

---

### Write Output Directly to File

```java
unifier.processToFile(
    new File("fr3d_output.json"),
    ToolType.FR3D,
    new File("out.bpseq"),
    true
);
```

---

### Parse from an InputStream

```java
try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile))) {
    String result = unifier.process(bis, ToolType.RNAPOLIS, true);
}
```

---

## 📄 Output Format

RNA2DUnifier produces two output variants:

**Extended BPSEQ** — one row per nucleotide, one column per Leontis–Westhof family:

```
Index	Nucleotide	cWW	tWW	cWH	tWH	cWS	tWS	cHH	tHH	cHS	tHS	cSS	tSS
1	G	29	0	0	0	0	0	0	0	0	0	0	0
2	G	28	0	0	0	0	0	0	0	0	0	0	0
5	U	25	0	0	0	0	0	0	0	0	35	0	0
```

**Canonical BPSEQ** — classic three-column format, Watson–Crick pairs only:

```
1	G	29
2	G	28
```

> `0` means no partner of that type. Multiple partners at one position are comma-separated (e.g., `3,17`).

---

## 🔬 Leontis–Westhof Nomenclature

All base-pair interactions are classified into the 12 geometric families:

| Code | Edges | Orientation |
|------|-------|-------------|
| cWW / tWW | Watson–Crick / Watson–Crick | cis / trans |
| cWH / tWH | Watson–Crick / Hoogsteen | cis / trans |
| cWS / tWS | Watson–Crick / Sugar Edge | cis / trans |
| cHH / tHH | Hoogsteen / Hoogsteen | cis / trans |
| cHS / tHS | Hoogsteen / Sugar Edge | cis / trans |
| cSS / tSS | Sugar Edge / Sugar Edge | cis / trans |

Canonical Watson–Crick pairs (A–U, G–C) are classified as **cWW**. The **tWW** type is also considered canonical by the library.

---

## 🔄 Tool Auto-Detection

When no `ToolType` is provided, RNA2DUnifier scans the first 4 096 bytes of the input for format-specific signatures:

| Signal in content | Detected tool |
|-------------------|--------------|
| `BEGIN_base-pair` | RNAview |
| `Residue conformations` | mc-annotate |
| `>` **and** `seq ` | RNApolis |
| JSON with `"annotations"` | FR3D |
| JSON with `"pairs"` | x3dna-DSSR |
| `N_INT_INT` residue pattern + LW annotation | Barnaba |
| `?` separator + `W:WC`-style tokens | bpnet |

> The stream must support `mark()`/`reset()`. Use `BufferedInputStream` when passing raw streams.

---

## 📊 Logging

The library uses **SLF4J + Logback**. All parsing listeners emit structured log messages during processing.

| Level | When it appears |
|-------|----------------|
| `TRACE` | Residue-to-index mapping details |
| `DEBUG` | Entry/exit of parse sections |
| `INFO` | End-of-parse summaries (sequence length, pair count) |
| `WARN` | Skipped residues, malformed tokens, unresolvable pairs |

Place a `logback.xml` on the classpath to configure verbosity:

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss} %-5level %logger{36} – %msg%n</pattern>
        </encoder>
    </appender>
    <logger name="it.unicam.cs.bdslab.rna2dunifier" level="WARN"/>
    <root level="WARN">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

---

## 🧪 Typical Use Cases

- Unifying RNA annotation datasets from heterogeneous tools
- Preprocessing interaction data for graph-based or ML pipelines
- Comparing base-pair annotations across different tools on the same structure
- Extracting specific LW interaction families for downstream analysis
- Batch conversion of tool outputs into a single normalized format

---

## ⚠️ Limitations

- Auto-detection reads only the first **4 096 bytes** — very short files may not be identified
- Nucleotide positions are **zero-based** internally; output is **1-based** (BPSEQ convention)
- Stacking interactions are parsed and stored but are **not exported** to BPSEQ
- Formats without explicit sequence data result in **`N` placeholders** in the output sequence
- Auto-detection requires a stream that supports **mark/reset** (`BufferedInputStream`)

---

## 🧱 Architecture (Simplified)

```text
RnaUnifier                    ← Main facade

parser/
  ToolType                    ← Enum of supported tools
  ParserFactory               ← Instantiates parsers, auto-detects tool
  RnaStructureParser          ← Common parser interface
  impl/                       ← One parser per tool

listeners/                    ← ANTLR tree-walk listeners (one per tool)

models/
  ExtendedRNASecondaryStructure
  Pair
  BondType

exporter/
  BpseqExporter               ← Converts model → BPSEQ / extended BPSEQ
```

---

## ❗ Error Handling

- `IOException` → file or stream could not be read
- `ParseException` → input does not conform to the expected grammar
- `IllegalArgumentException` → tool type cannot be auto-detected

---

## 🏛️ Project Information

- `Group`: it.unicam.cs.bdslab
- `Artifact`: rna2d-unifier
- `Version`: 0.0.1
- `Institution`: University of Camerino

---

## 📜 License
This project is licensed under the **Apache License, Version 2.0** – see the [LICENSE](LICENSE) file for details.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
