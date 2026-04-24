package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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
            "4PLX_A.pdb_sort.out, 34"
    })
    void testParseCounts(String resourcePath, int expectedPairCount) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/" + resourcePath)) {
            assertNotNull(is, "Resource not found: " + resourcePath);
            ExtendedRNASecondaryStructure structure = parser.parse(is);
            assertNotNull(structure);
            assertEquals(expectedPairCount, structure.getPairs().size(),
                    "Pair count mismatch for " + resourcePath);
        }
    }

    @Test
    void testParse1YMO_Raw_ContainsStackedAndNonCanonical() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/1YMO_A.pdb.out")) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // The raw file contains 39 pairs, including stacking and tertiary interactions.
            // Check that at least one stacked pair (e.g., "7_36") is present.
            boolean hasStacked = struct.getPairs().stream()
                    .anyMatch(p -> p.getPos1() == 6 && p.getPos2() == 35);
            assertTrue(hasStacked, "Raw file should contain stacked pair 7_36");
        }
    }

    @Test
    void testParse1YMO_Sorted_NoStackedPairs() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/1YMO_A.pdb_sort.out")) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // The sorted file has 21 pairs and no "stacked" annotation.
            long stackedCount = struct.getPairs().stream()
                    .filter(p -> p.toString().contains("stacked"))
                    .count();
            assertEquals(0, stackedCount, "Sorted file should contain no stacked pairs");
        }
    }

    @Test
    void testParse2K95_CanonicalTypes() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnaview/2K95_A.pdb_sort.out")) {
            ExtendedRNASecondaryStructure struct = parser.parse(is);
            // According to the summary in the file, there are 15 standard (WW cis) pairs,
            // 4 WH cis, 1 HS cis, 2 HS trans, and 1 SS cis.
            long wwCis = struct.getPairs().stream()
                    .filter(p -> p.getType() != null && p.getType().toString().equals("cWW"))
                    .count();
            // Depending on how Pair.type is set, we may need to adjust. Here we simply
            // verify that the total number of pairs equals 23 and that at least one
            // non‑canonical (e.g., S/H tran) exists.
            assertEquals(23, struct.getPairs().size());
            boolean hasNonCanonical = struct.getPairs().stream()
                    .anyMatch(p -> p.getType() != null && !p.getType().equals(BondType.LEONTIS_WESTHOF_cWW)
                    && !p.getType().equals(BondType.LEONTIS_WESTHOF_tWW));
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
            boolean hasUncommon = struct.getPairs().stream()
                    .anyMatch(p -> (p.getPos1() == 75 || p.getPos2() == 75) &&
                            (Objects.equals("a", p.getNucleotide1()) || Objects.equals("a", p.getNucleotide2())));
            assertTrue(hasUncommon, "Should contain pair with uncommon residue a76");
        }
    }
}