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

@DisplayName("BpnetParser – BPFIND output files")
class BpnetParserTest {

    private BpnetParser parser;

    @BeforeEach
    void setUp() {
        parser = new BpnetParser();
    }

    private InputStream resource(String resourceName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rna-output/bpnet/" + resourceName);
        assertNotNull(is, "Resource not found: " + resourceName);
        return is;
    }

    private boolean containsPair(List<Pair> pairs, int pos1, int pos2) {
        return pairs.stream().anyMatch(p -> (p.getPos1() == pos1 && p.getPos2() == pos2)
                || (p.getPos1() == pos2 && p.getPos2() == pos1));
    }

    // -------------------------------------------------------------------------
    // 1YMO_A.1YMO_A.out
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("1YMO_A – sequence")
    void test1YMO_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.1YMO_A.out"));
        assertEquals("GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAGUCAGCA", s.getSequence());
    }

    @Test
    @DisplayName("1YMO_A – total unique pairs")
    void test1YMO_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.1YMO_A.out"));
        assertEquals(21, s.getPairs().size());
    }

    @Test
    @DisplayName("1YMO_A – canonical (cWW) count")
    void test1YMO_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.1YMO_A.out"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(15, cww);
        assertEquals(cww, s.getCanonical().size());
    }

    @Test
    @DisplayName("1YMO_A – specific pairs")
    void test1YMO_A_specificPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("1YMO_A.1YMO_A.out"));
        assertTrue(containsPair(s.getPairs(), 0, 28));
        Pair p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 0 && pr.getPos2() == 28)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cWW, p.getType());

        assertTrue(containsPair(s.getPairs(), 4, 34));
        p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 4 && pr.getPos2() == 34)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cHS, p.getType());

        assertTrue(containsPair(s.getPairs(), 5, 35));
        p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 5 && pr.getPos2() == 35)).findFirst().get();
        assertNotEquals(BondType.UNKNOWN, p.getType());
    }

    // -------------------------------------------------------------------------
    // 2K95_A.2K95_A.out
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2K95_A – sequence")
    void test2K95_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.2K95_A.out"));
        assertEquals("GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAUGUCAGCA", s.getSequence());
    }

    @Test
    @DisplayName("2K95_A – total unique pairs")
    void test2K95_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.2K95_A.out"));
        assertEquals(24, s.getPairs().size());
    }

    @Test
    @DisplayName("2K95_A – canonical count")
    void test2K95_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.2K95_A.out"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(15, cww);
    }

    @Test
    @DisplayName("2K95_A – pair with S:HC (cHS)")
    void test2K95_A_cHS() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2K95_A.2K95_A.out"));
        assertTrue(containsPair(s.getPairs(), 4, 34));
        Pair p = s.getPairs().stream().filter(pr -> (pr.getPos1() == 4 && pr.getPos2() == 34)).findFirst().get();
        assertEquals(BondType.LEONTIS_WESTHOF_cHS, p.getType());
    }

    // -------------------------------------------------------------------------
    // 2M8K_A.2M8K_A.out
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2M8K_A – sequence")
    void test2M8K_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.2M8K_A.out"));
        assertEquals("GGUUUCUUUUUAGUGAUUUUUCCAAACCCCUUUGUGCAAAAAUCAUUA", s.getSequence());
    }

    @Test
    @DisplayName("2M8K_A – total unique pairs")
    void test2M8K_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.2M8K_A.out"));
        assertEquals(24, s.getPairs().size());
    }

    @Test
    @DisplayName("2M8K_A – canonical count")
    void test2M8K_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.2M8K_A.out"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(17, cww);
    }

    @Test
    @DisplayName("2M8K_A – non‑canonical w:sT")
    void test2M8K_A_nonCanonical() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("2M8K_A.2M8K_A.out"));
        assertTrue(containsPair(s.getPairs(), 3, 32));
    }

    // -------------------------------------------------------------------------
    // 4PLX_A.4PLX_A.out (con residui non comuni)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4PLX_A – sequence includes first char of uncommon residues")
    void test4PLX_A_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.4PLX_A.out"));
        assertEquals(76, s.getSequence().length());
        assertEquals('G', s.getSequence().charAt(0));   // GTP -> G
        assertEquals('A', s.getSequence().charAt(75)); // A23 -> A
    }

    @Test
    @DisplayName("4PLX_A – total unique pairs")
    void test4PLX_A_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.4PLX_A.out"));
        assertEquals(38, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_A – canonical count")
    void test4PLX_A_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_A.4PLX_A.out"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(25, cww);
    }

    // -------------------------------------------------------------------------
    // 4PLX_B.4PLX_B.out
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4PLX_B – sequence")
    void test4PLX_B_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.4PLX_B.out"));
        assertEquals(73, s.getSequence().length());
        assertEquals('G', s.getSequence().charAt(0));
        assertEquals('A', s.getSequence().charAt(72));
    }

    @Test
    @DisplayName("4PLX_B – total unique pairs")
    void test4PLX_B_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.4PLX_B.out"));
        assertEquals(35, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_B – canonical count")
    void test4PLX_B_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_B.4PLX_B.out"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(23, cww);
    }

    // -------------------------------------------------------------------------
    // 4PLX_C.4PLX_C.out
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4PLX_C – sequence")
    void test4PLX_C_sequence() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.4PLX_C.out"));
        assertEquals(71, s.getSequence().length());
        assertEquals('G', s.getSequence().charAt(0));
        assertEquals('A', s.getSequence().charAt(70));
    }

    @Test
    @DisplayName("4PLX_C – total unique pairs")
    void test4PLX_C_totalPairs() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.4PLX_C.out"));
        assertEquals(34, s.getPairs().size());
    }

    @Test
    @DisplayName("4PLX_C – canonical count")
    void test4PLX_C_canonicalCount() throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource("4PLX_C.4PLX_C.out"));
        long cww = s.getPairs().stream().filter(p -> p.getType() == BondType.LEONTIS_WESTHOF_cWW).count();
        assertEquals(22, cww);
    }

    @ParameterizedTest
    @CsvSource({
            "1YMO_A.1YMO_A.out",
            "2K95_A.2K95_A.out",
            "2M8K_A.2M8K_A.out",
            "4PLX_A.4PLX_A.out",
            "4PLX_B.4PLX_B.out",
            "4PLX_C.4PLX_C.out"
    })
    @DisplayName("No duplicate pairs (unordered)")
    void testNoDuplicatePairs(String resourceName) throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource(resourceName));
        for (Pair p : s.getPairs()) {
            for (Pair p2 : s.getPairs()) {
                if(p != p2) {
                    assertNotEquals(p, p2, "Duplicate pair " + p.toString() + " in " + resourceName);
                }
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1YMO_A.1YMO_A.out",
            "2K95_A.2K95_A.out",
            "2M8K_A.2M8K_A.out",
            "4PLX_A.4PLX_A.out",
            "4PLX_B.4PLX_B.out",
            "4PLX_C.4PLX_C.out"
    })
    @DisplayName("No UNKNOWN bond type for annotated pairs")
    void testNoUnknownBondTypes(String resourceName) throws Exception {
        ExtendedRNASecondaryStructure s = parser.parse(resource(resourceName));
        for (Pair p : s.getPairs()) {
            assertNotEquals(BondType.UNKNOWN, p.getType(),
                    "Pair " + p + " in " + resourceName + " has UNKNOWN type");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1YMO_A.1YMO_A.out",
            "2K95_A.2K95_A.out",
            "2M8K_A.2M8K_A.out",
            "4PLX_A.4PLX_A.out",
            "4PLX_B.4PLX_B.out",
            "4PLX_C.4PLX_C.out"
    })
    @DisplayName("All bond type annotations are correctly mapped")
    void testAllBondTypesCorrectlyMapped(String resourceName) throws Exception {
        ExtendedRNASecondaryStructure struct = parser.parse(resource(resourceName));

        // We need to re-parse the file to extract the raw annotation codes (e.g., "W:WC", "S:HC")
        // because the Pair object does not store the original annotation.
        // Instead, we'll read the file line by line, locate the annotation column,
        // and for each pair we find in the structure, verify its type matches the mapping.
        try (InputStream is = resource(resourceName)) {
            java.util.Scanner scanner = new java.util.Scanner(is);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Skip header lines and empty lines
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                // Split by whitespace; format example:
                // "     1       1   G ? A       29    29   C ? A    W:WC BP 0.37"
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 11) continue; // not a data line

                try {
                    // Parse serial numbers (1‑based)
                    int serial1 = Integer.parseInt(parts[0]);
                    int serial2 = Integer.parseInt(parts[6]);
                    // Convert to 0‑based indices (same as parser does)
                    int pos1 = serial1 - 1;
                    int pos2 = serial2 - 1;

                    // Extract annotation code (e.g., "W:WC")
                    String annotation = parts[10]; // column 11 in 0‑based indexing

                    // Find the corresponding Pair in the parsed structure
                    Pair pair = struct.getPairs().stream()
                            .filter(p -> (p.getPos1() == pos1 && p.getPos2() == pos2) ||
                                    (p.getPos1() == pos2 && p.getPos2() == pos1))
                            .findFirst()
                            .orElse(null);

                    if (pair != null) {
                        BondType expected = mapBpfindAnnotationToBondType(annotation);
                        assertEquals(expected, pair.getType(),
                                "Annotation " + annotation + " for pair (" + pos1 + "," + pos2 +
                                        ") mapped to " + pair.getType() + " but expected " + expected);
                    }
                    // If pair is null, it might be a stacking or tertiary interaction? Skip.
                } catch (NumberFormatException e) {
                    // Not a valid data line
                }
            }
            scanner.close();
        }
    }

    /**
     * Maps BPFIND annotation codes (e.g., "W:WC", "S:HC", "W:ST") to BondType.
     * This logic must match the parser's implementation exactly.
     */
    private BondType mapBpfindAnnotationToBondType(String annotation) {
        // Remove anything after space (e.g., "W:WC BP" -> "W:WC")
        String code = annotation.split("\\s+")[0];

        // Mapping table based on BPFIND output
        switch (code) {
            case "W:WC": return BondType.LEONTIS_WESTHOF_cWW;      // Watson-Crick cis
            case "W:ST": return BondType.LEONTIS_WESTHOF_tWS;      // Watson-Crick trans? Actually "W:ST" is Watson-Crick/Sugar trans -> tWS
            case "w:sC": return BondType.LEONTIS_WESTHOF_cWS;      // sugar edge cis
            case "w:sT": return BondType.LEONTIS_WESTHOF_tWS;      // sugar edge trans
            case "W:HC": return BondType.LEONTIS_WESTHOF_cWH;      // Watson-Crick/Hoogsteen cis
            case "H:WC": return BondType.LEONTIS_WESTHOF_cWH;      // Hoogsteen/Watson-Crick cis (same family)
            case "S:HC": return BondType.LEONTIS_WESTHOF_cHS;      // Sugar/Hoogsteen cis -> cHS
            case "H:SC": return BondType.LEONTIS_WESTHOF_cHS;      // Hoogsteen/Sugar cis -> cHS
            case "S:WT": return BondType.LEONTIS_WESTHOF_tWS;      // Sugar/Watson-Crick trans -> tWS
            case "+:HC": return BondType.LEONTIS_WESTHOF_cWH;      // non-standard but cis Watson‑Crick/Hoogsteen
            case "H:+C": return BondType.LEONTIS_WESTHOF_cWH;      // non-standard but cis Watson Crick/Hoogsteen
            case "s:sC": return BondType.LEONTIS_WESTHOF_cSS;      // sugar/sugar cis
            case "s:sT": return BondType.LEONTIS_WESTHOF_tSS;      // sugar/sugar trans
            case "h:wC": return BondType.LEONTIS_WESTHOF_cWH;      // Hoogsteen/Watson-Crick cis (same as H:WC)
            default:
                // If code is "W:??" or something else, fallback to UNKNOWN
                return BondType.UNKNOWN;
        }
    }
}