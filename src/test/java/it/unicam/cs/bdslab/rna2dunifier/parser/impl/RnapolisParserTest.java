package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RnapolisParser}.
 *
 * <p>Each test verifies that a given .3db file (RNApolis output) is parsed
 * into a correct {@link ExtendedRNASecondaryStructure}: sequence, total pair count,
 * canonical (cWW) pair count, and specific representative pairs.
 *
 * <p>Test files are loaded from the classpath (src/test/resources).
 * Expected values are derived by manually parsing the bracket-notation lines
 * in each file (stack-based algorithm, pseudoknot letter matching).
 */
@DisplayName("RnapolisParser – .3db file parsing")
class RnapolisParserTest {

    private RnapolisParser parser;

    @BeforeEach
    void setUp() {
        parser = new RnapolisParser();
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Opens a test-resource .3db file from the classpath.
     *
     * @param resourceName filename (e.g. "1YMO_A.3db")
     * @return the input stream
     */
    private InputStream resource(String resourceName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/rnapolis/" + resourceName);
        assertNotNull(is, "Test resource not found: " + resourceName);
        return is;
    }

    private InputStream resourceTest(String resourceName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("test/rnapolis/" + resourceName);
        assertNotNull(is, "Test resource not found: " + resourceName);
        return is;
    }

    /**
     * Checks whether the pair list contains a pair with the given positions (1-based).
     */
    private boolean containsPair(List<Pair> pairs, int left, int right) {
        return pairs.stream().anyMatch(p -> (p.getPos1() == left && p.getPos2() == right) ||
                (p.getPos2() == left && p.getPos1() == right));
    }

    // =========================================================================
    // 1YMO_A.3db
    // Strand: A  |  Sequence length: 47
    // Bond types present: cWW, cWH, cSH, tSW, tSH
    // Total pairs:     24
    // Canonical (cWW): 14
    // =========================================================================

    @Test
    @DisplayName("1YMO_A – sequence is correct")
    void test1YMO_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.3db"));
        assertEquals("GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAGUCAGCA", s.getSequence());
    }

    @Test
    @DisplayName("1YMO_A – sequence length is 47")
    void test1YMO_A_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.3db"));
        assertEquals(47, s.getSequence().length());
    }

    @Test
    @DisplayName("1YMO_A – total pairs count is 24")
    void test1YMO_A_totalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.3db"));
        assertEquals(24, s.getPairs().size());
    }

    @Test
    @DisplayName("1YMO_A – canonical (cWW) pairs count is 14")
    void test1YMO_A_canonicalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.3db"));
        assertEquals(14, s.getCanonical().size());
    }

    @Test
    @DisplayName("1YMO_A – first cWW stem (positions 1-6 paired with 24-29)")
    void test1YMO_A_firstStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 0, 28));
        assertTrue(containsPair(canonical, 1, 27));
        assertTrue(containsPair(canonical, 2, 26));
        assertTrue(containsPair(canonical, 3, 25));
        assertTrue(containsPair(canonical, 4, 24));
        assertTrue(containsPair(canonical, 5, 23));
    }

    @Test
    @DisplayName("1YMO_A – second cWW stem (positions 15-23 paired with 38-46)")
    void test1YMO_A_secondStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 14, 45));
        assertTrue(containsPair(canonical, 15, 44));
        assertTrue(containsPair(canonical, 17, 42));
        assertTrue(containsPair(canonical, 18, 41));
        assertTrue(containsPair(canonical, 19, 40));
        assertTrue(containsPair(canonical, 20, 39));
        assertTrue(containsPair(canonical, 21, 38));
        assertTrue(containsPair(canonical, 22, 37));
    }

    @Test
    @DisplayName("1YMO_A – non-canonical pairs are present in allPairs but not in canonical")
    void test1YMO_A_nonCanonicalPairsInAllButNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.3db"));
        // e.g. cWH (7,37)
        assertTrue(containsPair(s.getPairs(), 7, 37));
        assertFalse(containsPair(s.getCanonical(), 7, 37));
        // tSW (6, 36)
        assertTrue(containsPair(s.getPairs(), 6, 36));
        assertFalse(containsPair(s.getCanonical(), 6, 36));
    }

    // =========================================================================
    // 2K95_A.3db
    // Strand: A  |  Sequence length: 48
    // Bond types present: cWW, cWH, cSH, tHH
    // Total pairs:     21
    // Canonical (cWW): 15
    // =========================================================================

    @Test
    @DisplayName("2K95_A – sequence is correct")
    void test2K95_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.3db"));
        assertEquals("GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAUGUCAGCA", s.getSequence());
    }

    @Test
    @DisplayName("2K95_A – sequence length is 48")
    void test2K95_A_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.3db"));
        assertEquals(48, s.getSequence().length());
    }

    @Test
    @DisplayName("2K95_A – total pairs count is 27")
    void test2K95_A_totalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.3db"));
        assertEquals(27, s.getPairs().size());
    }

    @Test
    @DisplayName("2K95_A – canonical (cWW) pairs count is 15")
    void test2K95_A_canonicalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.3db"));
        assertEquals(15, s.getCanonical().size());
    }

    @Test
    @DisplayName("2K95_A – first cWW stem (positions 1-6 paired with 24-29)")
    void test2K95_A_firstStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 0, 28));
        assertTrue(containsPair(canonical, 1, 27));
        assertTrue(containsPair(canonical, 2, 26));
        assertTrue(containsPair(canonical, 3, 25));
        assertTrue(containsPair(canonical, 4, 24));
        assertTrue(containsPair(canonical, 5, 23));
    }

    @Test
    @DisplayName("2K95_A – second cWW stem (positions 15-23 paired with 38-47)")
    void test2K95_A_secondStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 14, 46));
        assertTrue(containsPair(canonical, 15, 45));
        assertTrue(containsPair(canonical, 16, 44));
        assertTrue(containsPair(canonical, 17, 43));
        assertTrue(containsPair(canonical, 18, 42));
        assertTrue(containsPair(canonical, 19, 41));
        assertTrue(containsPair(canonical, 20, 39));
        assertTrue(containsPair(canonical, 21, 38));
        assertTrue(containsPair(canonical, 22, 37));
    }

    @Test
    @DisplayName("2K95_A – tHH pair (13, 46) is in allPairs but not in canonical")
    void test2K95_A_tHHNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.3db"));
        assertTrue(containsPair(s.getPairs(), 12, 45));
        assertFalse(containsPair(s.getCanonical(), 12, 45));
    }

    // =========================================================================
    // 2M8K_A.3db
    // Strand: A  |  Sequence length: 48
    // Bond types present: cWW, cWH, cSH
    // Total pairs:     27
    // Canonical (cWW): 17
    // =========================================================================

    @Test
    @DisplayName("2M8K_A – sequence is correct")
    void test2M8K_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.3db"));
        assertEquals("GGUUUCUUUUUAGUGAUUUUUCCAAACCCCUUUGUGCAAAAAUCAUUA", s.getSequence());
    }

    @Test
    @DisplayName("2M8K_A – sequence length is 48")
    void test2M8K_A_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.3db"));
        assertEquals(48, s.getSequence().length());
    }

    @Test
    @DisplayName("2M8K_A – total pairs count is 27")
    void test2M8K_A_totalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.3db"));
        assertEquals(27, s.getPairs().size());
    }

    @Test
    @DisplayName("2M8K_A – canonical (cWW) pairs count is 17")
    void test2M8K_A_canonicalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.3db"));
        assertEquals(17, s.getCanonical().size());
    }

    @Test
    @DisplayName("2M8K_A – first cWW stem (1-5 paired with 24-28)")
    void test2M8K_A_firstStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 0, 27));
        assertTrue(containsPair(canonical, 1, 26));
        assertTrue(containsPair(canonical, 2, 25));
        assertTrue(containsPair(canonical, 3, 24));
        assertTrue(containsPair(canonical, 4, 23));
    }

    @Test
    @DisplayName("2M8K_A – second cWW stem with pseudoknots (22-23 paired with 34-36)")
    void test2M8K_A_pseudoknotStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 21, 35));
        assertTrue(containsPair(canonical, 22, 33));
    }

    @Test
    @DisplayName("2M8K_A – cWH pairs are non-canonical (in allPairs, not in canonical)")
    void test2M8K_A_cWHNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.3db"));
        // cWH: (6,36), (7,38)...
        assertTrue(containsPair(s.getPairs(), 5, 35));
        assertFalse(containsPair(s.getCanonical(), 5, 35));
        assertTrue(containsPair(s.getPairs(), 6, 37));
        assertFalse(containsPair(s.getCanonical(), 6, 37));
    }

    // =========================================================================
    // 4PLX_A.3db
    // Strand: A  |  Sequence length: 76
    // Bond types present: cWW, cWH, cSW, tSW, tSH
    // Total pairs:     37
    // Canonical (cWW): 24
    // =========================================================================

    @Test
    @DisplayName("4PLX_A – sequence is correct")
    void test4PLX_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        assertEquals("GGAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUCUUAAAAAAAAAAAAAGCAAAA",
                s.getSequence());
    }

    @Test
    @DisplayName("4PLX_A – sequence length is 76")
    void test4PLX_A_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        assertEquals(76, s.getSequence().length());
    }

    @Test
    @DisplayName("4PLX_A – total pairs count is 37")
    void test4PLX_A_totalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        assertEquals(37, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_A – canonical (cWW) pairs count is 24")
    void test4PLX_A_canonicalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        assertEquals(24, s.getCanonical().size());
    }

    @Test
    @DisplayName("4PLX_A – outer cWW pseudoknot stem (2-6 paired with 50-54)")
    void test4PLX_A_outerPseudoknotStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 1, 53));
        assertTrue(containsPair(canonical, 2, 52));
        assertTrue(containsPair(canonical, 3, 51));
        assertTrue(containsPair(canonical, 4, 50));
        assertTrue(containsPair(canonical, 5, 49));
    }

    @Test
    @DisplayName("4PLX_A – internal cWW stem (17-24 paired with 29-36)")
    void test4PLX_A_internalStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 16, 35));
        assertTrue(containsPair(canonical, 17, 34));
        assertTrue(containsPair(canonical, 18, 33));
        assertTrue(containsPair(canonical, 19, 32));
        assertTrue(containsPair(canonical, 20, 31));
        assertTrue(containsPair(canonical, 21, 30));
        assertTrue(containsPair(canonical, 22, 29));
        assertTrue(containsPair(canonical, 23, 28));
    }

    @Test
    @DisplayName("4PLX_A – 3' cWW stem (37-47 paired with 66-76)")
    void test4PLX_A_threePrimeStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 36, 75));
        assertTrue(containsPair(canonical, 37, 74));
        assertTrue(containsPair(canonical, 46, 65));
    }

    @Test
    @DisplayName("4PLX_A – cSW pair (51,64) is non-canonical")
    void test4PLX_A_cSWNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        assertTrue(containsPair(s.getPairs(), 50, 63));
        assertFalse(containsPair(s.getCanonical(), 50, 63));
    }

    @Test
    @DisplayName("4PLX_A – tSH pair (25,27) is non-canonical")
    void test4PLX_A_tSHNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.3db"));
        assertTrue(containsPair(s.getPairs(), 24, 26));
        assertFalse(containsPair(s.getCanonical(), 24, 26));
    }

    // =========================================================================
    // 4PLX_B.3db
    // Strand: B  |  Sequence length: 73
    // Bond types present: cWW, cWH, cSW, tSW, tSH
    // Total pairs:     36
    // Canonical (cWW): 23
    // =========================================================================

    @Test
    @DisplayName("4PLX_B – sequence is correct")
    void test4PLX_B_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        assertEquals("GGAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUCAAAAAAAAAAAAGCAAAA",
                s.getSequence());
    }

    @Test
    @DisplayName("4PLX_B – sequence length is 73")
    void test4PLX_B_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        assertEquals(73, s.getSequence().length());
    }

    @Test
    @DisplayName("4PLX_B – total pairs count is 36")
    void test4PLX_B_totalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        assertEquals(36, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_B – canonical (cWW) pairs count is 23")
    void test4PLX_B_canonicalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        assertEquals(23, s.getCanonical().size());
    }

    @Test
    @DisplayName("4PLX_B – outer cWW pseudoknot stem contains (2,54) and (7,63)")
    void test4PLX_B_outerStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 1, 53));
        assertTrue(containsPair(canonical, 6, 62));
    }

    @Test
    @DisplayName("4PLX_B – internal cWW stem (17-24 paired with 29-36)")
    void test4PLX_B_internalStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 16, 35));
        assertTrue(containsPair(canonical, 23, 28));
    }

    @Test
    @DisplayName("4PLX_B – 3' cWW stem contains (37,73) and (46,64)")
    void test4PLX_B_threePrimeStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 36, 72));
        assertTrue(containsPair(canonical, 45, 63));
    }

    @Test
    @DisplayName("4PLX_B – cSW pair (51,61) is non-canonical")
    void test4PLX_B_cSWNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.3db"));
        assertTrue(containsPair(s.getPairs(), 50, 60));
        assertFalse(containsPair(s.getCanonical(), 50, 60));
    }

    // =========================================================================
    // 4PLX_C.3db
    // Strand: C  |  Sequence length: 71
    // Bond types present: cWW, cWH, cSW, tSW, tSH
    // Total pairs:     35
    // Canonical (cWW): 23
    // =========================================================================

    @Test
    @DisplayName("4PLX_C – sequence is correct")
    void test4PLX_C_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        assertEquals("GGAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUAAAAAAAAAAAGCAAAA",
                s.getSequence());
    }

    @Test
    @DisplayName("4PLX_C – sequence length is 71")
    void test4PLX_C_sequenceLength() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        assertEquals(71, s.getSequence().length());
    }

    @Test
    @DisplayName("4PLX_C – total pairs count is 35")
    void test4PLX_C_totalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        assertEquals(35, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_C – canonical (cWW) pairs count is 23")
    void test4PLX_C_canonicalPairsCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        assertEquals(23, s.getCanonical().size());
    }

    @Test
    @DisplayName("4PLX_C – outer cWW stem contains (2,54), (3,53), (7,61)")
    void test4PLX_C_outerStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 1, 53));
        assertTrue(containsPair(canonical, 2, 52));
        assertTrue(containsPair(canonical, 6, 60));
    }

    @Test
    @DisplayName("4PLX_C – internal cWW stem (17-24 paired with 29-36)")
    void test4PLX_C_internalStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 16, 35));
        assertTrue(containsPair(canonical, 23, 28));
    }

    @Test
    @DisplayName("4PLX_C – 3' cWW stem contains (37,71) and (46,62)")
    void test4PLX_C_threePrimeStem() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        List<Pair> canonical = s.getCanonical();
        assertTrue(containsPair(canonical, 36, 70));
        assertTrue(containsPair(canonical, 45, 61));
    }

    @Test
    @DisplayName("4PLX_C – cSW pair (51,59) is non-canonical")
    void test4PLX_C_cSWNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        assertTrue(containsPair(s.getPairs(), 50, 58));
        assertFalse(containsPair(s.getCanonical(), 50, 58));
    }

    @Test
    @DisplayName("4PLX_C – tSH pair (25,27) is non-canonical")
    void test4PLX_C_tSHNotCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.3db"));
        assertTrue(containsPair(s.getPairs(), 24, 26));
        assertFalse(containsPair(s.getCanonical(), 24, 26));
    }

    @Test
    @DisplayName("only sequence")
    void testOnlySequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resourceTest("rnapolis_only_sequence.3db"));
        assertEquals("GUAUUGA", s.getSequence());
        assertEquals(0, s.getPairs().size());
    }

    @Test
    @DisplayName("Simple Pairs")
    void testSimplePairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resourceTest("rnapolis_simple_pairs.3db"));
        assertEquals("GCUGACAAACAGCA", s.getSequence());
        assertTrue(containsPair(s.getPairs(), 0, 13));
        assertTrue(containsPair(s.getPairs(), 1, 12));
        assertTrue(containsPair(s.getPairs(), 2, 11));
        assertTrue(containsPair(s.getPairs(), 3, 10));
        assertTrue(containsPair(s.getPairs(), 4, 9));
    }

    @Test
    @DisplayName("Complete")
    void testComplete() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resourceTest("rnapolis_complete.3db"));
        assertEquals("UUUUUCUUUUCGAAAAAAAGCAAAA", s.getSequence());
        assertEquals(1, s.getCanonical().size());
        assertTrue(containsPair(s.getPairs(),0,14));
        assertTrue(containsPair(s.getPairs(),2,16));
        assertTrue(containsPair(s.getPairs(),3,17));
    }
}