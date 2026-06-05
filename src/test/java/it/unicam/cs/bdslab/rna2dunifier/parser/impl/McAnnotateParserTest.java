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

package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import static org.junit.jupiter.api.Assertions.*;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("McAnnotateParser – mc-annotate output")
class McAnnotateParserTest {

    private McAnnotateParser parser;

    @BeforeEach
    void setUp() {
        parser = new McAnnotateParser();
    }

    private InputStream resource(String resourceName) {
        InputStream is = getClass()
            .getClassLoader()
            .getResourceAsStream("rna-output/mc-annotate/txt/" + resourceName);
        assertNotNull(is, "Resource not found: " + resourceName);
        return is;
    }

    private boolean containsPair(List<Pair> pairs, int pos1, int pos2) {
        return pairs
            .stream()
            .anyMatch(
                p -> (p.getPos1() == pos1 && p.getPos2() == pos2) || (p.getPos1() == pos2 && p.getPos2() == pos1)
            );
    }

    private Pair getPair(List<Pair> pairs, int pos1, int pos2) {
        return pairs
            .stream()
            .filter(p -> (p.getPos1() == pos1 && p.getPos2() == pos2) || (p.getPos1() == pos2 && p.getPos2() == pos1))
            .findFirst()
            .orElse(null);
    }

    // =========================================================================
    // 1YMO_A.txt
    // =========================================================================
    @Test
    @DisplayName("1YMO_A – sequence length 47")
    void test1YMO_A_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.txt"));
        assertEquals(47, s.getSequence().length());
        assertEquals('G', s.getSequence().charAt(0));
        assertEquals('A', s.getSequence().charAt(46));
    }

    @Test
    @DisplayName("1YMO_A – total base pairs (33)")
    void test1YMO_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.txt"));
        assertEquals(33, s.getPairs().size());
    }

    @Test
    @DisplayName("1YMO_A – canonical (cWW) count (16)")
    void test1YMO_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.txt"));
        long cww = s
            .getPairs()
            .stream()
            .filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW)
            .count();
        assertEquals(16, cww);
        assertEquals(cww, s.getCanonical().size());
    }

    @Test
    @DisplayName("1YMO_A – specific pairs and bond types")
    void test1YMO_A_specificPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.txt"));
        // A1-A29 -> indices 0,28 -> cWW
        assertTrue(containsPair(s.getPairs(), 0, 28));
        Pair p = getPair(s.getPairs(), 0, 28);
        assertEquals(BondType.LEONTIS_WESTHOF_cWW, p.getType());

        // A6-A36 -> indices 5,35 -> "G-A Ss/Ww pairing parallel trans X" -> tWS
        assertTrue(containsPair(s.getPairs(), 5, 35));
        p = getPair(s.getPairs(), 5, 35);
        assertEquals(BondType.LEONTIS_WESTHOF_tWS, p.getType());

        // A7-A37 -> indices 6,36 -> "U-A Ww/Hh pairing parallel cis XXIII" -> cWH
        assertTrue(containsPair(s.getPairs(), 6, 36));
        p = getPair(s.getPairs(), 6, 36);
        assertEquals(BondType.LEONTIS_WESTHOF_cWH, p.getType());
    }

    // =========================================================================
    // 2K95_A.txt
    // =========================================================================
    @Test
    @DisplayName("2K95_A – sequence length 48")
    void test2K95_A_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.txt"));
        assertEquals(48, s.getSequence().length());
    }

    @Test
    @DisplayName("2K95_A – total base pairs (35)")
    void test2K95_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.txt"));
        assertEquals(35, s.getPairs().size());
    }

    @Test
    @DisplayName("2K95_A – canonical count (15)")
    void test2K95_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.txt"));
        long cww = s
            .getPairs()
            .stream()
            .filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW)
            .count();
        assertEquals(15, cww);
    }

    @Test
    @DisplayName("2K95_A – tWS pair exists (A98-A172)")
    void test2K95_A_tWS() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.txt"));
        boolean hasTWS = s
            .getPairs()
            .stream()
            .anyMatch(p -> p.getType() == BondType.LEONTIS_WESTHOF_tWS);
        assertTrue(hasTWS);
    }

    // =========================================================================
    // 2M8K_A.txt
    // =========================================================================
    @Test
    @DisplayName("2M8K_A – sequence length 48")
    void test2M8K_A_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.txt"));
        assertEquals(48, s.getSequence().length());
    }

    @Test
    @DisplayName("2M8K_A – total base pairs (33)")
    void test2M8K_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.txt"));
        assertEquals(33, s.getPairs().size());
    }

    @Test
    @DisplayName("2M8K_A – canonical count (17)")
    void test2M8K_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.txt"));
        long cww = s
            .getPairs()
            .stream()
            .filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW)
            .count();
        assertEquals(17, cww);
    }

    // =========================================================================
    // 4PLX_A.txt (with uncommon residues GTP and A23)
    // =========================================================================
    @Test
    @DisplayName("4PLX_A – sequence length 76, uncommon residues mapped")
    void test4PLX_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.txt"));
        assertEquals(76, s.getSequence().length());
        assertEquals('N', s.getSequence().charAt(0)); // GTP → N
        assertEquals('N', s.getSequence().charAt(75)); // A23 → N
    }

    @Test
    @DisplayName("4PLX_A – total base pairs (41)")
    void test4PLX_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.txt"));
        assertEquals(41, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_A – canonical count (23)")
    void test4PLX_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.txt"));
        long cww = s
            .getPairs()
            .stream()
            .filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW)
            .count();
        assertEquals(23, cww);
    }

    // =========================================================================
    // 4PLX_B.txt
    // =========================================================================
    @Test
    @DisplayName("4PLX_B – sequence length 73")
    void test4PLX_B_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.txt"));
        assertEquals(73, s.getSequence().length());
    }

    @Test
    @DisplayName("4PLX_B – total base pairs (44)")
    void test4PLX_B_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.txt"));
        assertEquals(44, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_B – canonical count (22)")
    void test4PLX_B_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.txt"));
        long cww = s
            .getPairs()
            .stream()
            .filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW)
            .count();
        assertEquals(22, cww);
    }

    // =========================================================================
    // 4PLX_C.txt
    // =========================================================================
    @Test
    @DisplayName("4PLX_C – sequence length 71")
    void test4PLX_C_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.txt"));
        assertEquals(71, s.getSequence().length());
    }

    @Test
    @DisplayName("4PLX_C – total base pairs (38)")
    void test4PLX_C_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.txt"));
        assertEquals(38, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_C – canonical count (20)")
    void test4PLX_C_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.txt"));
        long cww = s
            .getPairs()
            .stream()
            .filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW)
            .count();
        assertEquals(20, cww);
    }

    // =========================================================================
    // Global consistency checks
    // =========================================================================
    @Test
    @DisplayName("All files – no duplicate pairs")
    void testNoDuplicatePairs() throws Exception {
        String[] files = { "1YMO_A.txt", "2K95_A.txt", "2M8K_A.txt", "4PLX_A.txt", "4PLX_B.txt", "4PLX_C.txt" };
        for (String file : files) {
            ExtendedRNASecondaryStructure s = parser.parse(resource(file));
            Set<String> keys = new HashSet<>();
            for (Pair p : s.getPairs()) {
                String key = Math.min(p.getPos1(), p.getPos2()) + "_" + Math.max(p.getPos1(), p.getPos2());
                assertFalse(keys.contains(key), "Duplicate pair in " + file + ": " + key);
                keys.add(key);
            }
        }
    }
}
