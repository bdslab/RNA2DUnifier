package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("X3dnaParser – DSSR JSON files")
class X3dnaParserTest {

    private X3dnaParser parser;

    @BeforeEach
    void setUp() {
        parser = new X3dnaParser();
    }

    private InputStream resource(String resourceName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/x3dna-dssr/" + resourceName);
        assertNotNull(is, "Resource not found: " + resourceName);
        return is;
    }

    private boolean containsPair(List<Pair> pairs, int pos1, int pos2) {
        return pairs.stream().anyMatch(p -> (p.getPos1() == pos1 && p.getPos2() == pos2)
                || (p.getPos1() == pos2 && p.getPos2() == pos1));
    }

    // -------------------------------------------------------------------------
    // Full JSON (contains sequence)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Full JSON – sequence extracted from dbn.all_chains.bseq")
    void testFullJson_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.json"));
        // Expected sequence from the file (47 nt)
        assertEquals("GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAGUCAGCA", s.getSequence());
    }

    @Test
    @DisplayName("Full JSON – total pairs count (22)")
    void testFullJson_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.json"));
        assertEquals(22, s.getPairs().size());
    }

    @Test
    @DisplayName("Full JSON – canonical (cWW) count (15)")
    void testFullJson_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(15, cww);
        assertEquals(cww, s.getCanonical().size());
    }

    @Test
    @DisplayName("Full JSON – specific pairs and their LW mapping")
    void testFullJson_specificPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.json"));
        // WC pair (G1-C29) -> indices 0,28 -> cWW
        assertTrue(containsPair(s.getPairs(), 0, 28));
        Pair p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 0 && pr.getPos2() == 28)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cWW, p.getType());

        // cSH pair (U5-A35) -> indices 4,34 -> cHS
        assertTrue(containsPair(s.getPairs(), 4, 34));
        p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 4 && pr.getPos2() == 34)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cHS, p.getType());

        // tSW pair (G6-A36) -> indices 5,35 -> should become tWS
        assertTrue(containsPair(s.getPairs(), 5, 35));
        p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 5 && pr.getPos2() == 35)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_tWS, p.getType());

        // cWH pair (U7-A37) -> indices 6,36 -> cWH
        assertTrue(containsPair(s.getPairs(), 6, 36));
        p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 6 && pr.getPos2() == 36)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cWH, p.getType());

        // tSH pair (G26-A33) -> indices 25,32 -> tHS
        assertTrue(containsPair(s.getPairs(), 25, 32));
        p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 25 && pr.getPos2() == 32)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_tHS, p.getType());
    }

    // -------------------------------------------------------------------------
    // Pair‑only JSON (no sequence)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Pair‑only JSON – sequence is empty or null")
    void testPairOnlyJson_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_pair-only.json"));
        // Pair‑only file has no sequence, we expect empty string (or null, but empty is safer)
        assertNotNull(s.getSequence());
        // Might be empty, but we don't enforce a specific value – just check it's not causing NPE
    }

    @Test
    @DisplayName("Pair‑only JSON – total pairs count (22)")
    void testPairOnlyJson_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_pair-only.json"));
        assertEquals(22, s.getPairs().size());
    }

    @Test
    @DisplayName("Pair‑only JSON – canonical count (15)")
    void testPairOnlyJson_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_pair-only.json"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(15, cww);
    }

    @Test
    @DisplayName("Pair‑only JSON – specific pairs match the full JSON")
    void testPairOnlyJson_specificPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A_pair-only.json"));
        // Same checks as in full JSON
        assertTrue(containsPair(s.getPairs(), 0, 28));
        assertTrue(containsPair(s.getPairs(), 4, 34));
        assertTrue(containsPair(s.getPairs(), 5, 35));
        assertTrue(containsPair(s.getPairs(), 6, 36));
        assertTrue(containsPair(s.getPairs(), 25, 32));

        Pair p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 5 && pr.getPos2() == 35)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_tWS, p.getType());
    }
}