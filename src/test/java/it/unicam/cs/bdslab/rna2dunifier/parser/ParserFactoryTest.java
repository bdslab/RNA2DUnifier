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

package it.unicam.cs.bdslab.rna2dunifier.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test for {@link ParserFactory}.
 */
@DisplayName("ParserFactory")
class ParserFactoryTest {

    // ------------------------------------------------------------------ //
    //  getParser                                                         //
    // ------------------------------------------------------------------ //

    @ParameterizedTest(name = "getParser({0}) return non-null parser")
    @EnumSource(ToolType.class)
    @DisplayName("getParser – every ToolType has an implementation")
    void getParserNotNull(ToolType type) {
        RnaStructureParser parser = ParserFactory.getParser(type);
        assertNotNull(parser, "getParser should not return null for " + type);
    }

    @Test
    @DisplayName("getParser – different types return different instances")
    void getParserReturnsDifferentInstances() {
        List<RnaStructureParser> parsers = new ArrayList<>();
        for (ToolType type : ToolType.values()) {
            parsers.add(ParserFactory.getParser(type));
        }

        for (int i = 0; i < parsers.size(); i++) {
            RnaStructureParser parser = parsers.get(i);
            for (int j = i + 1; j < parsers.size(); j++) {
                RnaStructureParser parser2 = parsers.get(j);
                assertNotSame(parser, parser2);
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  detectTool                                                        //
    // ------------------------------------------------------------------ //

    private static InputStream toBuffered(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new BufferedInputStream(new ByteArrayInputStream(bytes));
    }

    @Test
    @DisplayName("detectTool – signature RNAview (BEGIN_base-pair)")
    void detectRnaview() throws IOException {
        String content =
            "PDB data file name: test.pdb\n" + "BEGIN_base-pair\n" + "1_2 A 1 G A 10 C cWW +/+\n" + "END_base-pair\n";
        assertEquals(ToolType.RNAVIEW, ParserFactory.detectTool(toBuffered(content)));
    }

    @Test
    @DisplayName("detectTool – Signature McAnnotate (Residue conformations)")
    void detectMcAnnotate() throws IOException {
        String content = "Residue conformations ---\n" + "A1 : G C3p_endo anti\n";
        assertEquals(ToolType.MCANNOTATE, ParserFactory.detectTool(toBuffered(content)));
    }

    @Test
    @DisplayName("detectTool – Signature RNApolis (>strand + seq)")
    void detectRnapolis() throws IOException {
        String content = ">strand_A\n" + "seq GCAUGCAU\n" + "cWW [[[....]]]\n";
        assertEquals(ToolType.RNAPOLIS, ParserFactory.detectTool(toBuffered(content)));
    }

    @Test
    @DisplayName("detectTool – Signature FR3D (JSON with key \"annotations\")")
    void detectFr3d() throws IOException {
        String content = "{\n" + "  \"pdb_id\": \"1YMO\",\n" + "  \"annotations\": []\n" + "}\n";
        assertEquals(ToolType.FR3D, ParserFactory.detectTool(toBuffered(content)));
    }

    @Test
    @DisplayName("detectTool – Signature X3DNA (JSON with key \"pairs\")")
    void detectX3dna() throws IOException {
        String content = "{\n" + "  \"num_pairs\": 5,\n" + "  \"pairs\": []\n" + "}\n";
        assertEquals(ToolType.X3DNA, ParserFactory.detectTool(toBuffered(content)));
    }

    @Test
    @DisplayName("detectTool – Signature Barnaba (nucleotide_INT_INT + annotazione LW)")
    void detectBarnaba() throws IOException {
        String content = "# comment\n" + "G_1_0      C_10_0      WCc\n" + "A_2_0      U_9_0       WCc\n";
        assertEquals(ToolType.BARNABA, ParserFactory.detectTool(toBuffered(content)));
    }

    @Test
    @DisplayName("detectTool – Signature Bpnet (? separator + W:WC bond)")
    void detectBpnet() throws IOException {
        String content = "#HEADER info\n" + "     1       1   G ? A       29    29   C ? A    W:WC BP 0.37\n";
        assertEquals(ToolType.BPNET, ParserFactory.detectTool(toBuffered(content)));
    }

    @Test
    @DisplayName("detectTool – unknown stream throws IllegalArgumentException")
    void detectUnknownThrows() {
        String garbage = "not supported format\nrow2\nrow3\n";
        assertThrows(IllegalArgumentException.class, () -> ParserFactory.detectTool(toBuffered(garbage)));
    }

    @Test
    @DisplayName("detectTool – the stream get resetted after the read (positions not changed)")
    void detectToolResetsStream() throws IOException {
        String content = "BEGIN_base-pair\nsome data\nEND_base-pair\n";
        InputStream stream = toBuffered(content);

        ParserFactory.detectTool(stream);

        // Dopo la detect, lo stream deve poter essere letto dall'inizio
        byte[] remaining = stream.readAllBytes();
        String reconstituted = new String(remaining, StandardCharsets.UTF_8);
        assertTrue(reconstituted.contains("BEGIN_base-pair"), "The stream should be resetted after detectTool");
    }
}
