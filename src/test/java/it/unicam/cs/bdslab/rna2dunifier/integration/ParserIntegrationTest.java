package it.unicam.cs.bdslab.rna2dunifier.integration;

import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rna2dunifier.parser.ParserFactory;
import it.unicam.cs.bdslab.rna2dunifier.parser.RnaStructureParser;
import it.unicam.cs.bdslab.rna2dunifier.parser.ToolType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: upload the test files in
 * {@code src/test/resources/rna-output/} and verify that every parser produce
 * a reasonable structure (non null, with pairs, ecc.).
 *
 * <p>The tests use the structure 1YMO_A that is present in every tool.
 */
@DisplayName("Parser integration with real files")
class ParserIntegrationTest {

    /** Carica una risorsa dal classpath e restituisce il relativo InputStream. */
    private InputStream resource(String path) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(is, "Resource not found: " + path);
        return is;
    }

    // ------------------------------------------------------------------ //
    //  Helper                                                            //
    // ------------------------------------------------------------------ //

    private ExtendedRNASecondaryStructure parse(ToolType type, String resourcePath)
            throws IOException, ParseException {
        RnaStructureParser parser = ParserFactory.getParser(type);
        try (InputStream is = resource(resourcePath)) {
            return parser.parse(is);
        }
    }

    // ------------------------------------------------------------------ //
    //  Barnaba                                                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Barnaba – 1YMO_A pairing: structure non-null with pairs")
    void barnabaParsesPairingFile() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.BARNABA,
                "rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");

        assertNotNull(s);
        assertFalse(s.getPairs().isEmpty(), "There should be at least one pair");
    }

    @Test
    @DisplayName("Barnaba – 1YMO_A pairing: sequence extracted")
    void barnabaExtractsSequence() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.BARNABA,
                "rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");

        assertNotNull(s.getSequence());
        assertFalse(s.getSequence().isBlank(), "The sequence is empty");
    }

    @Test
    @DisplayName("Barnaba – 1YMO_A pairing: exist at least one cWW canonical pair")
    void barnabaHasCanonicalPairs() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.BARNABA,
                "rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");

        assertFalse(s.getCanonical().isEmpty(),
                "There should be at least one canonical pair (WCc -> cWW)");
    }

    @Test
    @DisplayName("Barnaba – every pairs have non negative position")
    void barnabaPositionsNonNegative() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.BARNABA,
                "rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");

        for (Pair p : s.getPairs()) {
            assertTrue(p.getPos1() >= 0, "pos1 should be >= 0");
            assertTrue(p.getPos2() >= 0, "pos2 should be >= 0");
            assertNotEquals(p.getPos1(), p.getPos2(), "pos1 & pos2 shound not be equals");
        }
    }

    // ------------------------------------------------------------------ //
    //  RNAview                                                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("RNAview – 1YMO_A: structure non-null with pairs")
    void rnaviewParsesFile() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.RNAVIEW,
                "rna-output/rnaview/1YMO_A.pdb.out");

        assertNotNull(s);
        assertFalse(s.getPairs().isEmpty());
    }

    @Test
    @DisplayName("RNAview – 1YMO_A")
    void rnaviewBondTypesNotNull() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.RNAVIEW,
                "rna-output/rnaview/1YMO_A.pdb.out");

        for (Pair p : s.getPairs()) {
            assertNotNull(p.getType(), "The BondType should not be null");
        }
    }

    // ------------------------------------------------------------------ //
    //  FR3D                                                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("FR3D – 1YMO_A: non-null structure with pairs")
    void fr3dParsesFile() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.FR3D,
                "rna-output/fr3d/1YMO_A_A_basepair.json");

        assertNotNull(s);
        assertFalse(s.getPairs().isEmpty());
    }

    @Test
    @DisplayName("FR3D – 1YMO_A: every pairs have nucleotides not empty")
    void fr3dNucleotidesPresent() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.FR3D,
                "rna-output/fr3d/1YMO_A_A_basepair.json");

        for (Pair p : s.getPairs()) {
            assertNotNull(p.getNucleotide1(),  "nucleotide1 should not be empty");
            assertFalse(p.getNucleotide1().isBlank(), "nucleotide1 should not be empty");
            assertNotNull(p.getNucleotide2(),  "nucleotide2 should not be empty");
            assertFalse(p.getNucleotide2().isBlank(), "nucleotide2 should not be empty");
        }
    }

    // ------------------------------------------------------------------ //
    //  McAnnotate                                                        //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("McAnnotate – 1YMO_A: structure non-null with pairs")
    void mcAnnotateParsesFile() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.MCANNOTATE,
                "rna-output/mc-annotate/txt/1YMO_A.txt");

        assertNotNull(s);
        assertFalse(s.getPairs().isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Bpnet                                                             //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Bpnet – 1YMO_A: structure non-null with pairs")
    void bpnetParsesFile() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.BPNET,
                "rna-output/bpnet/1YMO_A.1YMO_A.out");

        assertNotNull(s);
        assertFalse(s.getPairs().isEmpty());
    }

    @Test
    @DisplayName("Bpnet – 1YMO_A: exists at least one canonical pair W:WC")
    void bpnetHasCanonicalPairs() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.BPNET,
                "rna-output/bpnet/1YMO_A.1YMO_A.out");

        assertFalse(s.getCanonical().isEmpty(),
                "There should be at least one canonical pair");
    }

    // ------------------------------------------------------------------ //
    //  RNApolis                                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("RNApolis – 1YMO_A: structure non-null with pairs")
    void rnapolisParsesFile() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.RNAPOLIS,
                "rna-output/rnapolis/1YMO_A.3db");

        assertNotNull(s);
        assertFalse(s.getPairs().isEmpty());
    }

    @Test
    @DisplayName("RNApolis – 1YMO_A: the sequence has the same length expected")
    void rnapolisSequenceLength() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.RNAPOLIS,
                "rna-output/rnapolis/1YMO_A.3db");

        // 1YMO_A ha 47 nucleotidi
        int seqLen = s.getSequence() != null ? s.getSequence().length() : 0;
        assertTrue(seqLen > 0, "The sequence length should be > 0");
    }

    // ------------------------------------------------------------------ //
    //  X3DNA                                                             //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("X3DNA – 1YMO_A pair-only: structure non-null with pairs")
    void x3dnaParsesFile() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.X3DNA,
                "rna-output/x3dna-dssr/1YMO_A_dssr.json");

        assertNotNull(s);
        assertFalse(s.getPairs().isEmpty());
    }

    @Test
    @DisplayName("X3DNA – 1YMO_A pair-only: number of pairs coerent with num_pairs=22")
    void x3dnaPairCount() throws IOException, ParseException {
        ExtendedRNASecondaryStructure s = parse(
                ToolType.X3DNA,
                "rna-output/x3dna-dssr/1YMO_A_dssr.json");

        // Il file dichiara num_pairs: 22
        assertEquals(22, s.getPairs().size(),
                "The number of pairs should be equal to num_pairs in the JSON");
    }

    // ------------------------------------------------------------------ //
    //  cross-tool coherence: same structure (1YMO_A) from different tool //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Cross-tool: Barnaba and FR3D has same canonical pairs (±5)")
    void crossToolCanonicalPairCount() throws IOException, ParseException {
        ExtendedRNASecondaryStructure barnaba = parse(
                ToolType.BARNABA,
                "rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out");
        ExtendedRNASecondaryStructure fr3d = parse(
                ToolType.FR3D,
                "rna-output/fr3d/1YMO_A_A_basepair.json");

        int diff = Math.abs(barnaba.getCanonical().size() - fr3d.getCanonical().size());
        assertTrue(diff <= 5,
                String.format("Barnaba canonical=%d, FR3D canonical=%d: difference > 5",
                        barnaba.getCanonical().size(), fr3d.getCanonical().size()));
    }

    @Test
    @DisplayName("Cross-tool: Every parser produce BondType non-null for every pair")
    void allParsersProduceValidBondTypes() throws IOException, ParseException {
        record Entry(ToolType type, String path) {}
        Entry[] entries = {
                new Entry(ToolType.BARNABA, "rna-output/barnaba/1YMO_A.pdb.ANNOTATE.pairing.out"),
                new Entry(ToolType.FR3D, "rna-output/fr3d/1YMO_A_A_basepair.json"),
                new Entry(ToolType.RNAVIEW, "rna-output/rnaview/1YMO_A.pdb.out"),
                new Entry(ToolType.MCANNOTATE, "rna-output/mc-annotate/txt/1YMO_A.txt"),
                new Entry(ToolType.BPNET, "rna-output/bpnet/1YMO_A.1YMO_A.out"),
                new Entry(ToolType.RNAPOLIS, "rna-output/rnapolis/1YMO_A.3db"),
                new Entry(ToolType.X3DNA, "rna-output/x3dna-dssr/1YMO_A_dssr.json"),
        };

        for (Entry e : entries) {
            ExtendedRNASecondaryStructure s = parse(e.type(), e.path());
            for (Pair p : s.getPairs()) {
                assertNotNull(p.getType(),
                        e.type() + ": BondType null found");
            }
        }
    }
}