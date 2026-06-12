/*
 * Copyright 2026 Francesco Palozzi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unicam.cs.bdslab.rna2dunifier.exporter;

import static org.junit.jupiter.api.Assertions.*;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link BpseqExporter}.
 */
@DisplayName("BpseqExporter")
class BpseqExporterTest {

    private BpseqExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new BpseqExporter();
    }

    // ------------------------------------------------------------------ //
    //  printCanonicalBPSEQ                                               //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("canonical BPSEQ: empty structure produce empty string")
    void canonicalEmptyStructure() {
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder().setSequence("").build();
        String result = exporter.printCanonicalBPSEQ(s);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("canonical BPSEQ: a pair cWW produce two rows with correct indexes")
    void canonicalSinglePair() {
        // pos 0 (G) - pos 5 (C), cWW => indici 1-based: 1 e 6
        Pair p = new Pair(0, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
            .setSequence("GAAAAC")
            .addPair(p)
            .build();

        String result = exporter.printCanonicalBPSEQ(s);
        String[] lines = result.strip().split("\n");

        // Devono esserci esattamente 2 righe (le posizioni 1 e 6 sono appaiate)
        assertEquals(2, lines.length);

        // prima riga: posizione 1, G, partner 6
        String[] col1 = lines[0].split(" ");
        assertEquals("1", col1[0]);
        assertEquals("G", col1[1]);
        assertEquals("6", col1[2]);

        // seconda riga: posizione 6, C, partner 1
        String[] col2 = lines[1].split(" ");
        assertEquals("6", col2[0]);
        assertEquals("C", col2[1]);
        assertEquals("1", col2[2]);
    }

    @Test
    @DisplayName("canonical BPSEQ: non-canonical pair not in the output")
    void canonicalIgnoresNonCanonicalPairs() {
        Pair nonCanonical = new Pair(0, 5, "G", "A", BondType.LEONTIS_WESTHOF_cWH);
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
            .setSequence("GAAAAA")
            .addPair(nonCanonical)
            .build();

        String result = exporter.printCanonicalBPSEQ(s);
        assertTrue(result.isEmpty(), "Non-canonical pairs should not appear in the canonical BPSEQ");
    }

    @Test
    @DisplayName("canonical BPSEQ: two canonical pairs should produce 4 rows")
    void canonicalTwoPairs() {
        Pair p1 = new Pair(0, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair p2 = new Pair(1, 4, "A", "U", BondType.LEONTIS_WESTHOF_cWW);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
            .setSequence("GAAACG") // lunghezza 6
            .addPair(p1)
            .addPair(p2)
            .build();

        String result = exporter.printCanonicalBPSEQ(s);
        long lineCount = result
            .lines()
            .filter(l -> !l.isBlank())
            .count();
        assertEquals(4, lineCount);
    }

    // ------------------------------------------------------------------ //
    //  printExtendedBPSEQ – header                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("extended BPSEQ: the first row is the header with the 12 bond types LW")
    void extendedBpseqHeader() {
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder().setSequence("A").build();

        String result = exporter.printExtendedBPSEQ(s);
        String header = result.lines().findFirst().orElse("");

        assertTrue(header.contains("id"), "Header should contain 'Index'");
        assertTrue(header.contains("nt"), "Header should contain 'Nucleotide'");
        assertTrue(header.contains("cWW"), "Header should contain cWW");
        assertTrue(header.contains("tWW"), "Header should contain tWW");
        assertTrue(header.contains("cWH"), "Header should contain cWH");
        assertTrue(header.contains("tWH"), "Header should contain tWH");
        assertTrue(header.contains("cWS"), "Header should contain cWS");
        assertTrue(header.contains("tWS"), "Header should contain tWS");
        assertTrue(header.contains("cHH"), "Header should contain cHH");
        assertTrue(header.contains("tHH"), "Header should contain tHH");
        assertTrue(header.contains("cHS"), "Header should contain cHS");
        assertTrue(header.contains("tHS"), "Header should contain tHS");
        assertTrue(header.contains("cSS"), "Header should contain cSS");
        assertTrue(header.contains("tSS"), "Header should contain tSS");
    }

    @Test
    @DisplayName("extended BPSEQ: every data row has 2 + 12 = 14 tab-separated column")
    void extendedBpseqColumnCount() {
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder().setSequence("GAUC").build();

        String result = exporter.printExtendedBPSEQ(s);
        // La prima riga è l'intestazione; le successive sono dati
        result
            .lines()
            .skip(1)
            .filter(l -> !l.isBlank())
            .forEach(line -> {
                String[] cols = line.split("\\s+");
                assertEquals(
                    14,
                    cols.length,
                    "Every data row should have 14 column (Index + Nucleotide + 12 bond types LW): " + line
                );
            });
    }

    @Test
    @DisplayName("extended BPSEQ: position without pair has 0 for all LW bond type")
    void extendedBpseqUnpairedPosition() {
        // Nessuna coppia -> tutti 0
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder().setSequence("ACGU").build();

        String result = exporter.printExtendedBPSEQ(s);
        result
            .lines()
            .skip(1)
            .filter(l -> !l.isBlank())
            .forEach(line -> {
                String[] cols = line.split("\t");
                for (int i = 2; i < cols.length; i++) {
                    assertEquals(
                        "0",
                        cols[i],
                        "Position without pair should have 0 for all LW bond type, row: " + line
                    );
                }
            });
    }

    @Test
    @DisplayName("extended BPSEQ: cWW pair produce the correct partner in the column cWW")
    void extendedBpseqCWWPartner() {
        // pos 0 (G) appaiata a pos 3 (C) con cWW
        Pair p = new Pair(0, 3, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
            .setSequence("GAUC")
            .addPair(p)
            .build();

        String result = exporter.printExtendedBPSEQ(s);
        // La riga della posizione 1 (indice 0) deve avere "4" nella colonna cWW (indice 2)
        String firstDataLine = result.lines().skip(1).findFirst().orElse("");
        String[] cols = firstDataLine.split("\\s+");

        assertEquals("1", cols[0], "Index should be 1");
        assertEquals("G", cols[1], "Nucleotide should be G");
        assertEquals("4", cols[2], "Column cWW should indicate the partner 4 (1-based)");
    }

    @Test
    @DisplayName("extended BPSEQ: the number of row data is equal to the length of the sequence")
    void extendedBpseqRowCountMatchesSequenceLength() {
        String seq = "GCAUGCAU";
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder().setSequence(seq).build();

        String result = exporter.printExtendedBPSEQ(s);
        long dataLines = result
            .lines()
            .skip(1)
            .filter(l -> !l.isBlank())
            .count();
        assertEquals(seq.length(), dataLines);
    }

    // ------------------------------------------------------------------ //
    //  Reconstruction of the sequence from the nucleotides of the pairs  //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("extended BPSEQ: without explicit sequence reconstruct 'N' in the unknown positions")
    void extendedBpseqReconstructSequenceWithN() {
        // Coppia tra pos 0 e pos 2 con nucleotidi noti; pos 1 non è in nessuna coppia
        Pair p = new Pair(0, 2, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder().addPair(p).build();

        String result = exporter.printExtendedBPSEQ(s);
        // Riga indice 2 (pos 1): nucleotide deve essere N
        String secondLine = result.lines().skip(2).findFirst().orElse("");
        String[] cols = secondLine.split("\\s+");
        assertEquals("N", cols[1], "The position without nucleotide should be 'N'");
    }

    // ------------------------------------------------------------------ //
    //  Pair with multiple partner of the same type                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("extended BPSEQ: position with two partner cWW listed with comma")
    void extendedBpseqMultiplePartnersCommaList() {
        // pos 0 appaiata sia con pos 3 che con pos 5 entrambe cWW
        Pair p1 = new Pair(0, 3, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair p2 = new Pair(0, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
            .setSequence("GAUCUC")
            .addPair(p1)
            .addPair(p2)
            .build();

        String result = exporter.printExtendedBPSEQ(s);
        String firstDataLine = result.lines().skip(1).findFirst().orElse("");
        String[] cols = firstDataLine.split("\\s+");

        // La colonna cWW (indice 2) deve contenere "4,6" o "6,4"
        String cWWCol = cols[2];
        assertTrue(
            cWWCol.contains("4") && cWWCol.contains("6") && cWWCol.contains(","),
            "Two cWW partner should be listed separately by a comma: " + cWWCol
        );
    }
}
