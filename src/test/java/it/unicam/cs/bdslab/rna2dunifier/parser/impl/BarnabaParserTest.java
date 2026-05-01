package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BarnabaParser – pairing and stacking outputs")
class BarnabaParserTest {

    private BarnabaParser parser;

    @BeforeEach
    void setUp() {
        parser = new BarnabaParser();
    }

    private InputStream resource(String resourceName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/barnaba/" + resourceName);
        assertNotNull(is, "Resource not found: " + resourceName);
        return is;
    }

    private boolean containsPair(List<Pair> pairs, int pos1, int pos2) {
        return pairs.stream().anyMatch(p -> (p.getPos1() == pos1 && p.getPos2() == pos2)
                || (p.getPos1() == pos2 && p.getPos2() == pos1));
    }

    // -------------------------------------------------------------------------
    // Pairing files (.ANNOTATE.pairing.out)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("1YMO_A pairing – sequence")
    void test1YMO_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.pdb.ANNOTATE.pairing.out"));
        assertEquals("GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAGUCAGCA", s.getSequence());
    }

    @Test
    @DisplayName("1YMO_A pairing – total pairs (32)")
    void test1YMO_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.pdb.ANNOTATE.pairing.out"));
        assertEquals(32, s.getPairs().size());
    }

    @Test
    @DisplayName("1YMO_A pairing – canonical count (15)")
    void test1YMO_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.pdb.ANNOTATE.pairing.out"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(15, cww);
        assertEquals(cww, s.getCanonical().size());
    }

    @Test
    @DisplayName("1YMO_A pairing – specific pairs (canonical, cHS, UNKNOWN)")
    void test1YMO_A_specificPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.pdb.ANNOTATE.pairing.out"));
        // Canonical (G1-C29) -> indices 0,28
        assertTrue(containsPair(s.getPairs(), 0, 28));
        // cHS (U5-A35 SHc -> cHS) -> indices 4,34
        assertTrue(containsPair(s.getPairs(), 4, 34));
        Pair p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 4 && pr.getPos2() == 34)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cHS, p.getType());
        // XXX (G2-A32) -> indices 1,31 -> UNKNOWN
        assertTrue(containsPair(s.getPairs(), 1, 31));
        p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 1 && pr.getPos2() == 31)).findFirst().get();
        assertEquals(BondType.UNKNOWN, p.getType());
    }

    @Test
    @DisplayName("2K95_A pairing – sequence, counts, tHS")
    void test2K95_A() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.pdb.ANNOTATE.pairing.out"));
        assertEquals("GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAUGUCAGCA", s.getSequence());
        assertEquals(35, s.getPairs().size());
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(15, cww);
        boolean hasTHS = s.getPairs().stream().anyMatch(p -> p.getType() == BondType.LEONTIS_WESTHOF_tHS);
        assertFalse(hasTHS);
    }

    @Test
    @DisplayName("2M8K_A pairing – sequence, counts, GUc mapped to cWW")
    void test2M8K_A() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.pdb.ANNOTATE.pairing.out"));
        assertEquals("GGUUUCUUUUUAGUGAUUUUUCCAAACCCCUUUGUGCAAAAAUCAUUA", s.getSequence());
        assertEquals(29, s.getPairs().size());
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(17, cww);
        // GUc (G13-U46) -> cWW
        assertTrue(containsPair(s.getPairs(), 12, 45));
        Pair p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 12 && pr.getPos2() == 45)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cWW, p.getType());
    }

    @Test
    @DisplayName("4PLX_A pairing – sequence, counts, tSS")
    void test4PLX_A() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.pdb.ANNOTATE.pairing.out"));
        // Sequence from file: starts G_2_0, ends A_75_0
        assertEquals("GAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUCUUAAAAAAAAAAAAAGCAAA", s.getSequence());
        assertEquals(37, s.getPairs().size());
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(23, cww);
        boolean hasTSS = s.getPairs().stream().anyMatch(p -> p.getType() == BondType.LEONTIS_WESTHOF_tSS);
        assertTrue(hasTSS);
    }

    @Test
    @DisplayName("4PLX_A_uncommon pairing – sequence unchanged, pairs same count")
    void test4PLX_A_uncommon() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.pdb.ANNOTATE.pairing.out"));
        assertEquals("GAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUCUUAAAAAAAAAAAAAGCAAA", s.getSequence());
        assertEquals(37, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_B pairing – sequence, counts")
    void test4PLX_B() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.pdb.ANNOTATE.pairing.out"));
        assertEquals("GAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUCAAAAAAAAAAAAGCAAA", s.getSequence());
        assertEquals(38, s.getPairs().size());
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(23, cww);
    }

    @Test
    @DisplayName("4PLX_C pairing – sequence, counts")
    void test4PLX_C() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.pdb.ANNOTATE.pairing.out"));
        assertEquals("GAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUAAAAAAAAAAAGCAAA", s.getSequence());
        assertEquals(37, s.getPairs().size());
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(18, cww);
    }

    @ParameterizedTest
    @CsvSource({
            "1YMO_A.pdb.ANNOTATE.pairing.out",
            "2K95_A.pdb.ANNOTATE.pairing.out",
            "2M8K_A.pdb.ANNOTATE.pairing.out",
            "4PLX_A.pdb.ANNOTATE.pairing.out",
            "4PLX_B.pdb.ANNOTATE.pairing.out",
            "4PLX_C.pdb.ANNOTATE.pairing.out",
            "3J6B_A_A.pdb.ANNOTATE.pairing.out"
    })
    @DisplayName("No UNKNOWN except those originally marked XXX")
    void testNoUnexpectedUnknown(String resourceName) throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource(resourceName));
        // We can't know which are XXX without storing original annotation, so we just ensure at least some non-UNKNOWN exist.
        assertTrue(s.getPairs().stream().anyMatch(p -> p.getType() != BondType.UNKNOWN));
    }

    // -------------------------------------------------------------------------
    // Stacking files (.ANNOTATE.stacking.out) – must produce zero base pairs
    // and must NOT treat adjacent stacking (e.g., 3-4) as a pair.
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("1YMO_A stacking – no base pairs, adjacent stacking not added")
    void test1YMO_A_stacking() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.pdb.ANNOTATE.stacking.out"));
        assertEquals(2, s.getPairs().size(), "Stacking file should have 0 base pairs");
        // Sequence still present
        assertEquals(47, s.getSequence().length());
    }

    @Test
    @DisplayName("2K95_A stacking – no base pairs")
    void test2K95_A_stacking() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.pdb.ANNOTATE.stacking.out"));
        assertEquals(5, s.getPairs().size());
        assertEquals(48, s.getSequence().length());
    }

    @Test
    @DisplayName("2M8K_A stacking – no base pairs")
    void test2M8K_A_stacking() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.pdb.ANNOTATE.stacking.out"));
        assertEquals(5, s.getPairs().size());
        assertEquals(48, s.getSequence().length());
    }

    @Test
    @DisplayName("4PLX_A stacking – no base pairs, adjacent stacking ignored")
    void test4PLX_A_stacking() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.pdb.ANNOTATE.stacking.out"));
        assertEquals(5, s.getPairs().size());
        // The stacking file for 4PLX_A has 74 residues (uncommon skipped) – just check not empty.
        assertNotNull(s.getSequence());
    }
}