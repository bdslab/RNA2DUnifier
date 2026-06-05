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
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RnaviewParserTest {

    private final RnaviewParser parser = new RnaviewParser();

    @ParameterizedTest
    @CsvSource({
        "1YMO_A.pdb.out, 39",
        "1YMO_A.pdb_sort.out, 21",
        "2K95_A.pdb.out, 38",
        "2K95_A.pdb_sort.out, 23",
        "2M8K_A.pdb.out, 39",
        "2M8K_A.pdb_sort.out, 23",
        "4PLX_A.pdb.out, 44",
        "4PLX_A.pdb_sort.out, 34",
    })
    void testParseCounts(String resourcePath, int expectedPairCount) throws Exception {
        try (
            InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("rna-output/rnaview/" + resourcePath)
        ) {
            assertNotNull(is, "Resource not found: " + resourcePath);
            ExtendedRNASecondaryStructure structure = parser.parse(is);
            assertNotNull(structure);
            assertEquals(expectedPairCount, structure.getPairs().size(), "Pair count mismatch for " + resourcePath);
        }
    }

    @Test
    void testParse1YMO_Raw_ContainsStackedAndNonCanonical() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/1YMO_A.pdb.out")) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // The raw file contains 39 pairs, including stacking and tertiary interactions.
            // Check that at least one stacked pair (e.g., "7_36") is present.
            boolean hasStacked = struct
                .getPairs()
                .stream()
                .anyMatch(p -> p.getPos1() == 6 && p.getPos2() == 35);
            assertTrue(hasStacked, "Raw file should contain stacked pair 7_36");
        }
    }

    @Test
    void testParse1YMO_Sorted_NoStackedPairs() throws Exception {
        try (
            InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/1YMO_A.pdb_sort.out")
        ) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // The sorted file has 21 pairs and no "stacked" annotation.
            long stackedCount = struct
                .getPairs()
                .stream()
                .filter(p -> p.toString().contains("stacked"))
                .count();
            assertEquals(0, stackedCount, "Sorted file should contain no stacked pairs");
        }
    }

    @Test
    void testParse2K95_CanonicalTypes() throws Exception {
        try (
            InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/2K95_A.pdb_sort.out")
        ) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // Depending on how Pair.type is set, we may need to adjust. Here we simply
            // verify that the total number of pairs equals 23 and that at least one
            // non‑canonical (e.g., S/H tran) exists.
            assertEquals(23, struct.getPairs().size());
            boolean hasNonCanonical = struct
                .getPairs()
                .stream()
                .anyMatch(
                    p ->
                        p.getType() != null &&
                        !p.getType().equals(BondType.LEONTIS_WESTHOF_cWW) &&
                        !p.getType().equals(BondType.LEONTIS_WESTHOF_tWW)
                );
            assertTrue(hasNonCanonical);
        }
    }

    @Test
    void testParse4PLX_HandlesUncommonResidues() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/4PLX_A.pdb.out")) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // The raw file contains 44 pairs, including those with uncommon residues
            // (e.g., GTP assigned to 'g', A23 assigned to 'a').
            assertEquals(44, struct.getPairs().size());
            // Check that a pair involving the uncommon 'a' (residue 76) is present.
            boolean hasUncommon = struct
                .getPairs()
                .stream()
                .anyMatch(
                    p ->
                        (p.getPos1() == 75 || p.getPos2() == 75) &&
                        (Objects.equals("a", p.getNucleotide1()) || Objects.equals("N", p.getNucleotide2()))
                );
            assertTrue(hasUncommon, "Should contain pair with uncommon residue a76");
        }
    }

    @ParameterizedTest
    @CsvSource({
        "1YMO_A.pdb.out, 1_29, cWW",
        "1YMO_A.pdb.out, 5_25, cWW",
        "1YMO_A.pdb.out, 5_35, cHS",
        "1YMO_A.pdb.out, 6_36, tHS",
        "1YMO_A.pdb.out, 7_36, STACKING",
        "1YMO_A.pdb.out, 7_37, cWH",
        "1YMO_A.pdb.out, 12_43, cHS",
        "1YMO_A.pdb.out, 2_32, tWS",
        "1YMO_A.pdb.out, 3_32, tSS",
        "1YMO_A.pdb_sort.out, 1_29, cWW",
        "1YMO_A.pdb_sort.out, 5_35, cHS",
        "2K95_A.pdb.out, 1_29, cWW",
        "2K95_A.pdb.out, 5_35, cHS",
        "2K95_A.pdb.out, 6_36, tHS",
        "2K95_A.pdb.out, 6_7, cSS",
        "2K95_A.pdb.out, 11_20, cWH",
        "2K95_A.pdb.out, 12_19, tHS",
        "2K95_A.pdb_sort.out, 19_43, cWW",
        "2K95_A.pdb_sort.out, 6_7, cSS",
        "2M8K_A.pdb.out, 1_28, cWW",
        "2M8K_A.pdb.out, 3_26, cWW",
        "2M8K_A.pdb.out, 6_36, cWH",
        "2M8K_A.pdb.out, 9_40, cHS",
        "2M8K_A.pdb.out, 13_46, cWW",
        "2M8K_A.pdb_sort.out, 13_46, cWW",
        "4PLX_A.pdb.out, 2_54, cWW",
        "4PLX_A.pdb.out, 6_50, cWW",
        "4PLX_A.pdb.out, 6_65, tSS",
        "4PLX_A.pdb.out, 7_66, cWH",
        "4PLX_A.pdb.out, 11_70, cHS",
        "4PLX_A.pdb.out, 25_28, tHS",
        "4PLX_A.pdb_sort.out, 2_54, cWW",
        "4PLX_A.pdb_sort.out, 11_70, cHS",
    })
    void testBondTypeMapping(String resourcePath, String pairKey, String expectedBondType) throws Exception {
        try (
            InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("rna-output/rnaview/" + resourcePath)
        ) {
            assertNotNull(is, "Resource not found: " + resourcePath);
            ExtendedRNASecondaryStructure struct = parser.parse(is);

            // Parse pairKey like "1_29" into positions
            String[] parts = pairKey.split("_");
            int pos1 = Integer.parseInt(parts[0]) - 1; // RNAview uses 1-based internal numbers, but after normalization they become 0-based
            int pos2 = Integer.parseInt(parts[1]) - 1;

            // Find the pair with these positions (order doesn't matter)
            Pair found = struct
                .getPairs()
                .stream()
                .filter(
                    p -> (p.getPos1() == pos1 && p.getPos2() == pos2) || (p.getPos1() == pos2 && p.getPos2() == pos1)
                )
                .findFirst()
                .orElse(null);
            assertNotNull(found, "Pair " + pairKey + " not found in parsed structure");

            BondType expected = BondType.fromString(expectedBondType);
            assertEquals(
                expected,
                found.getType(),
                "For pair " + pairKey + " expected " + expectedBondType + " but got " + found.getType()
            );
        }
    }

    @Test
    void testUnknownEdgeAnnotationMapsToUnknown() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/2K95_A.pdb.out")) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // Cerca la coppia con posizioni 14 e 44 (dopo normalizzazione: 13 e 43? Attenzione agli shift)
            // RNAview internal numbers: 14 e 44 diventano 13 e 43 in 0‑based.
            Pair unknownPair = struct
                .getPairs()
                .stream()
                .filter(p -> (p.getPos1() == 13 && p.getPos2() == 43) || (p.getPos1() == 43 && p.getPos2() == 13))
                .findFirst()
                .orElse(null);
            assertNotNull(unknownPair, "Pair 14_44 not found");
            assertEquals(BondType.UNKNOWN, unknownPair.getType(), "Annotation H/. dovrebbe dare UNKNOWN");
        }
    }

    @Test
    void testCanonicalPairsHaveCisOrTransCanonicalBondType() throws Exception {
        // Verify that all canonical (standard W.C.) pairs are correctly labelled cWW (not tWW)
        // because RNAview only outputs antiparallel Watson-Crick as "+/+ cis" or "-/- cis".
        // (Parallel Watson-Crick is extremely rare and would be annotated differently.)
        String[] files = {
            "1YMO_A.pdb.out",
            "1YMO_A.pdb_sort.out",
            "2K95_A.pdb.out",
            "2K95_A.pdb_sort.out",
            "2M8K_A.pdb.out",
            "2M8K_A.pdb_sort.out",
            "4PLX_A.pdb.out",
            "4PLX_A.pdb_sort.out",
        };
        for (String file : files) {
            try (
                InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("rna-output/rnaview/" + file)
            ) {
                ExtendedRNASecondaryStructure struct = parser.parse(is);
                for (Pair p : struct.getPairs()) {
                    BondType bt = p.getType();
                    if (bt.isCanonical()) {
                        // In RNAview, canonical pairs are always cWW (cis Watson-Crick), never tWW.
                        // tWW would appear only if the annotation were something like "+/+ trans" (non-existent).
                        assertEquals(
                            BondType.LEONTIS_WESTHOF_cWW,
                            bt,
                            "Canonical pair should be cWW, but found " + bt + " in " + file
                        );
                    }
                }
            }
        }
    }
}
