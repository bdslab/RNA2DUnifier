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

package it.unicam.cs.bdslab.rna2dunifier.integration;

import static org.junit.jupiter.api.Assertions.*;

import it.unicam.cs.bdslab.rna2dunifier.RnaUnifier;
import it.unicam.cs.bdslab.rna2dunifier.parser.ToolType;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-End integration test for {@link RnaUnifier}.
 *
 * <p>Verify that the complete pipeline(parse → export) produce an output
 * BPSEQ valid from real example files
 */
@DisplayName("RnaUnifier – end-to-end integration")
class RnaUnifierIntegrationTest {

    private RnaUnifier unifier;

    @BeforeEach
    void setUp() {
        unifier = new RnaUnifier();
    }

    // ------------------------------------------------------------------ //
    //  Utility                                                           //
    // ------------------------------------------------------------------ //

    /** Risolve un percorso di risorsa come File. */
    private File resourceFile(String path) {
        URL url = getClass().getClassLoader().getResource(path);
        assertNotNull(url, "Resource not found: " + path);
        return new File(url.getFile());
    }

    /** Verifica che l'output extended BPSEQ abbia un'intestazione corretta. */
    private void assertValidExtendedBpseqHeader(String output) {
        String header = output.lines().findFirst().orElse("");
        assertTrue(header.startsWith("Index"), "Header should start with 'Index'");
        assertTrue(header.contains("cWW"), "Header should contain 'cWW'");
        assertTrue(header.contains("tSS"), "Header should contain 'tSS'");
    }

    /** Verifica che ogni riga dati abbia esattamente 14 colonne. */
    private void assertDataLinesHave14Columns(String output) {
        output
            .lines()
            .skip(1)
            .filter(l -> !l.isBlank())
            .forEach(line -> {
                String[] cols = line.split("\t");
                assertEquals(14, cols.length, "Every row should have 14 columns: " + line);
            });
    }

    // ------------------------------------------------------------------ //
    //  process(File, ToolType, boolean) – extended                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("process(File, BARNABA, extended=true) produce BPSEQ extended valid")
    void processBarnabaExtended() throws IOException, ParseException {
        File f = resourceFile("rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");
        String result = unifier.process(f, ToolType.BARNABA, true);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertValidExtendedBpseqHeader(result);
        assertDataLinesHave14Columns(result);
    }

    @Test
    @DisplayName("process(File, FR3D, extended=true) produce BPSEQ extended valid")
    void processFr3dExtended() throws IOException, ParseException {
        File f = resourceFile("rna-output/fr3d/1YMO_A_A_basepair.json");
        String result = unifier.process(f, ToolType.FR3D, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
        assertDataLinesHave14Columns(result);
    }

    @Test
    @DisplayName("process(File, RNAVIEW, extended=true) produce BPSEQ extended valid")
    void processRnaviewExtended() throws IOException, ParseException {
        File f = resourceFile("rna-output/rnaview/1YMO_A.pdb.out");
        String result = unifier.process(f, ToolType.RNAVIEW, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    @Test
    @DisplayName("process(File, MCANNOTATE, extended=true) produce BPSEQ extended valid")
    void processMcAnnotateExtended() throws IOException, ParseException {
        File f = resourceFile("rna-output/mc-annotate/txt/1YMO_A.txt");
        String result = unifier.process(f, ToolType.MCANNOTATE, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    @Test
    @DisplayName("process(File, BPNET, extended=true) produce BPSEQ extended valid")
    void processBpnetExtended() throws IOException, ParseException {
        File f = resourceFile("rna-output/bpnet/1YMO_A.1YMO_A.out");
        String result = unifier.process(f, ToolType.BPNET, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    @Test
    @DisplayName("process(File, RNAPOLIS, extended=true) produce BPSEQ extended valid")
    void processRnapolisExtended() throws IOException, ParseException {
        File f = resourceFile("rna-output/rnapolis/1YMO_A.3db");
        String result = unifier.process(f, ToolType.RNAPOLIS, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    @Test
    @DisplayName("process(File, X3DNA, extended=true) produce BPSEQ extended valid")
    void processX3dnaExtended() throws IOException, ParseException {
        File f = resourceFile("rna-output/x3dna-dssr/1YMO_A_dssr.json");
        String result = unifier.process(f, ToolType.X3DNA, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    // ------------------------------------------------------------------ //
    //  process(File, ToolType, boolean) – canonical                      //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("process(File, BARNABA, extended=false) produce BPSEQ canonical withou header")
    void processBarnabaCanonical() throws IOException, ParseException {
        File f = resourceFile("rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");
        String result = unifier.process(f, ToolType.BARNABA, false);

        assertNotNull(result);
        assertFalse(result.isBlank());
        // Il canonical BPSEQ non ha un'intestazione con i tipi LW
        assertFalse(result.startsWith("Index"), "The canonical BPSEQ should not start with header");
        // Ogni riga deve avere esattamente 3 colonne
        result
            .lines()
            .filter(l -> !l.isBlank())
            .forEach(line -> {
                String[] cols = line.split("\t");
                assertEquals(3, cols.length, "Every row of canonical BPSEQ should have 3 columns: " + line);
            });
    }

    @Test
    @DisplayName("process canonical: the positions are 1-based and positive")
    void canonicalPositionsOneBased() throws IOException, ParseException {
        File f = resourceFile("rna-output/fr3d/1YMO_A_A_basepair.json");
        String result = unifier.process(f, ToolType.FR3D, false);

        result
            .lines()
            .filter(l -> !l.isBlank())
            .forEach(line -> {
                String[] cols = line.split("\t");
                int idx = Integer.parseInt(cols[0]);
                int partner = Integer.parseInt(cols[2]);
                assertTrue(idx >= 1, "Index should be >= 1: " + line);
                assertTrue(partner >= 1, "Partner should be >= 1: " + line);
            });
    }

    // ------------------------------------------------------------------ //
    //  process(File, boolean) – auto-detect                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("process(File, extended=true) with auto-detect on file RNAview")
    void processAutoDetectRnaview() throws IOException, ParseException {
        File f = resourceFile("rna-output/rnaview/1YMO_A.pdb.out");
        String result = unifier.process(f, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    @Test
    @DisplayName("process(File, extended=true) with auto-detect on file FR3D JSON")
    void processAutoDetectFr3d() throws IOException, ParseException {
        File f = resourceFile("rna-output/fr3d/1YMO_A_A_basepair.json");
        String result = unifier.process(f, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    @Test
    @DisplayName("process(File, extended=true) with auto-detect on file X3DNA JSON")
    void processAutoDetectX3dna() throws IOException, ParseException {
        File f = resourceFile("rna-output/x3dna-dssr/1YMO_A_dssr.json");
        String result = unifier.process(f, true);

        assertNotNull(result);
        assertValidExtendedBpseqHeader(result);
    }

    // ------------------------------------------------------------------ //
    //  processToFile                                                     //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("processToFile write the output on destination file")
    void processToFileWritesOutput(@TempDir Path tempDir) throws IOException, ParseException {
        File input = resourceFile("rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");
        File output = tempDir.resolve("output.ebpseq").toFile();

        unifier.processToFile(input, ToolType.BARNABA, output, true);

        assertTrue(output.exists(), "The output file should be created");
        assertTrue(output.length() > 0, "The output file should not be empty");
    }

    @Test
    @DisplayName("processToFile: the file content is the same of process()")
    void processToFileContentMatchesProcess(@TempDir Path tempDir) throws IOException, ParseException {
        File input = resourceFile("rna-output/fr3d/1YMO_A_A_basepair.json");
        File output = tempDir.resolve("out.ebpseq").toFile();

        String direct = unifier.process(input, ToolType.FR3D, true);
        unifier.processToFile(input, ToolType.FR3D, output, true);
        String fromFile = new String(java.nio.file.Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8);

        assertEquals(direct, fromFile, "The written content on the file should be the same of the direct output");
    }

    @Test
    @DisplayName("processToFile with auto-detect create the file correctly")
    void processToFileAutoDetect(@TempDir Path tempDir) throws IOException, ParseException {
        File input = resourceFile("rna-output/rnaview/1YMO_A.pdb.out");
        File output = tempDir.resolve("auto.ebpseq").toFile();

        unifier.processToFile(input, output, true);

        assertTrue(output.exists());
        String content = new String(java.nio.file.Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8);
        assertValidExtendedBpseqHeader(content);
    }

    // ------------------------------------------------------------------ //
    //  process(InputStream, ToolType, boolean)                            //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("process(InputStream, BARNABA, extended=true) produce BPSEQ valid")
    void processInputStream() throws IOException, ParseException {
        try (
            InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("rna-output/barnaba/2K95_A.pdb.ANNOTATE.pairing.out")
        ) {
            assertNotNull(is);
            String result = unifier.process(is, ToolType.BARNABA, true);
            assertNotNull(result);
            assertValidExtendedBpseqHeader(result);
        }
    }

    // ------------------------------------------------------------------ //
    //  Invariant: extended contains more information of canonical       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Extended output contains more rows/character than the canonical output")
    void extendedLongerThanCanonical() throws IOException, ParseException {
        File f = resourceFile("rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");
        String extended = unifier.process(f, ToolType.BARNABA, true);
        String canonical = unifier.process(f, ToolType.BARNABA, false);

        assertTrue(
            extended.length() > canonical.length(),
            "Extended output should be more long than the canonical output"
        );
    }
}
