package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fr3dParser – FR3D JSON output")
class Fr3dParserTest {

    private Fr3dParser parser;

    @BeforeEach
    void setUp() {
        parser = new Fr3dParser();
    }

    private InputStream resource(String resourceName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/fr3d/" + resourceName);
        assertNotNull(is, "Resource not found: " + resourceName);
        return is;
    }

    private boolean containsPair(List<Pair> pairs, int pos1, int pos2) {
        return pairs.stream().anyMatch(p -> (p.getPos1() == pos1 && p.getPos2() == pos2)
                || (p.getPos1() == pos2 && p.getPos2() == pos1));
    }

    private Pair getPair(List<Pair> pairs, int pos1, int pos2) {
        return pairs.stream()
                .filter(p -> (p.getPos1() == pos1 && p.getPos2() == pos2) ||
                        (p.getPos1() == pos2 && p.getPos2() == pos1))
                .findFirst()
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // 1YMO_A – distinct seq_ids sorted: [1,2,3,4,5,6,7,8,9,10,15,16,18,19,20,21,22,23,24,25,26,27,28,29,37,38,39,40,41,42,43,44,45,46]
    // Mapping: 19→13, 42→29; 10→9, 40→27
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("1YMO_A – total pairs count (18)")
    void test1YMO_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_A_basepair.json"));
        assertEquals(18, s.getPairs().size());
    }

    @Test
    @DisplayName("1YMO_A – canonical count (14)")
    void test1YMO_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_A_basepair.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(14, cww);
        assertEquals(cww, s.getCanonical().size());
    }

    @Test
    @DisplayName("1YMO_A – specific pairs")
    void test1YMO_A_specificPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_A_basepair.json"));
        // cWW (19,42) -> indices 13,29
        assertTrue(containsPair(s.getPairs(), 13, 29));
        Pair p = getPair(s.getPairs(), 13, 29);
        assertNotNull(p);
        assertEquals(BondType.LEONTIS_WESTHOF_cWW, p.getType());

        // cWH (10,40) -> indices 9,27
        assertTrue(containsPair(s.getPairs(), 9, 27));
        p = getPair(s.getPairs(), 9, 27);
        assertEquals(BondType.LEONTIS_WESTHOF_cWH, p.getType());
    }

    @Test
    @DisplayName("1YMO_A – no duplicate pairs")
    void test1YMO_A_noDuplicates() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_A_basepair.json"));
        Set<String> keys = new HashSet<>();
        for (Pair p : s.getPairs()) {
            String key = Math.min(p.getPos1(), p.getPos2()) + "_" + Math.max(p.getPos1(), p.getPos2());
            assertFalse(keys.contains(key), "Duplicate pair: " + key);
            keys.add(key);
        }
    }

    // -------------------------------------------------------------------------
    // 2K95_A – distinct seq_ids: 35 residues (93..102,107..121,173..176,178..183)
    // Mapping: 116→19, 173→25
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2K95_A – total pairs count (20)")
    void test2K95_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A_A_basepair.json"));
        assertEquals(20, s.getPairs().size());
    }

    @Test
    @DisplayName("2K95_A – canonical count (15)")
    void test2K95_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A_A_basepair.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(15, cww);
    }

    @Test
    @DisplayName("2K95_A – cSW maps to cWS")
    void test2K95_A_cSWtoCWS() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A_A_basepair.json"));
        // cSW (116,173) -> indices (19,25)
        assertTrue(containsPair(s.getPairs(), 19, 25));
        Pair p = getPair(s.getPairs(), 19, 25);
        assertNotNull(p);
        assertEquals(BondType.LEONTIS_WESTHOF_cWS, p.getType());
    }

    // -------------------------------------------------------------------------
    // 2M8K_A – only counts (no specific index checks)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2M8K_A – total pairs count (23)")
    void test2M8K_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A_A_basepair.json"));
        assertEquals(23, s.getPairs().size());
    }

    @Test
    @DisplayName("2M8K_A – canonical count (17)")
    void test2M8K_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A_A_basepair.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(17, cww);
    }

    @Test
    @DisplayName("2M8K_A – cWH pairs count (6)")
    void test2M8K_A_cWH() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A_A_basepair.json"));
        long cwh = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWH).count();
        assertEquals(6, cwh);
    }

    // -------------------------------------------------------------------------
    // 4PLX_A – distinct seq_ids: 51 residues (1..13,15..24,37..47,50..54,65..76)
    // Mapping: seq_id 37 → 23, 76 → 50; 6 → 5, 65 → 39
    // Total annotations: 34 pairs; canonical (cWW): 24
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4PLX_A – total pairs count (34)")
    void test4PLX_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A_A_basepair.json"));
        assertEquals(34, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_A – canonical count (24)")
    void test4PLX_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A_A_basepair.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(24, cww);
        assertEquals(cww, s.getCanonical().size());
    }

    @Test
    @DisplayName("4PLX_A – modified residues handled correctly")
    void test4PLX_A_modifiedResidues() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A_A_basepair.json"));
        // Pair (37,76) -> indices (23,50)
        assertTrue(containsPair(s.getPairs(), 31, 58));
        Pair p = getPair(s.getPairs(), 31, 58);
        assertNotNull(p);
        // Check that modified residue A23 (seq_id=76) has nucleotide 'A'
        if (p.getPos1() == 50) {
            assertEquals("A", p.getNucleotide1());
        } else if (p.getPos2() == 50) {
            assertEquals("A", p.getNucleotide2());
        }
    }

    @Test
    @DisplayName("4PLX_A – tSS bond type")
    void test4PLX_A_tSS() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A_A_basepair.json"));
        long tss = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_tSS).count();
        assertEquals(1, tss);
        // tSS pair (6,65) -> indices (5,39)
        assertTrue(containsPair(s.getPairs(), 5, 47));
        Pair p = getPair(s.getPairs(), 5, 47);
        assertEquals(BondType.LEONTIS_WESTHOF_tSS, p.getType());
    }

    // -------------------------------------------------------------------------
    // 4PLX_B – only counts (no specific index tests)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4PLX_B – total pairs count (30)")
    void test4PLX_B_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B_B_basepair.json"));
        assertEquals(31, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_B – canonical count (24)")
    void test4PLX_B_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B_B_basepair.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(21, cww);
    }

    // -------------------------------------------------------------------------
    // 4PLX_C – only counts (no specific index tests)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4PLX_C – total pairs count (24)")
    void test4PLX_C_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C_C_basepair.json"));
        assertEquals(30, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_C – canonical count (18)")
    void test4PLX_C_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C_C_basepair.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(21, cww);
    }

    // -------------------------------------------------------------------------
    // Global: no UNKNOWN bond types
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("All bond types in files are recognized (no UNKNOWN)")
    void testNoUnknownBondTypes() throws Exception {
        String[] files = {
                "1YMO_A_A_basepair.json",
                "2K95_A_A_basepair.json",
                "2M8K_A_A_basepair.json",
                "4PLX_A_A_basepair.json",
                "4PLX_B_B_basepair.json",
                "4PLX_C_C_basepair.json"
        };
        for (String file : files) {
            ExtendedRNASecondaryStructure s = parser.parse(resource(file));
            for (Pair p : s.getPairs()) {
                assertNotEquals(BondType.UNKNOWN, p.getType(),
                        "Unknown bond type in " + file + " for pair " + p);
            }
        }
    }
}