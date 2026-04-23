package it.unicam.cs.bdslab.rna2dunifier.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link ExtendedRNASecondaryStructure}.
 */
@DisplayName("ExtendedRNASecondaryStructure")
class ExtendedRNASecondaryStructureTest {

    // ------------------------------------------------------------------ //
    //  Builder                                                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Builder with sequence and without any pair produce empty structure")
    void builderEmptyPairs() {
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .setSequence("ACGU")
                .build();

        assertEquals("ACGU", s.getSequence());
        assertTrue(s.getPairs().isEmpty());
        assertTrue(s.getCanonical().isEmpty());
    }

    @Test
    @DisplayName("addPair add the pair in the general list")
    void addPairGoesToAllPairs() {
        Pair p = new Pair(0, 3, "G", "C", BondType.LEONTIS_WESTHOF_cWW);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .setSequence("GAUC")
                .addPair(p)
                .build();

        assertEquals(1, s.getPairs().size());
        assertTrue(s.getPairs().contains(p));
    }

    // ------------------------------------------------------------------ //
    //  Separation canonical / non-canonical                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("addPair with cWW added also to the canonical list")
    void canonicalPairAddedToCanonicalList() {
        Pair canonical = new Pair(0, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .setSequence("GACCUG")
                .addPair(canonical)
                .build();

        assertEquals(1, s.getPairs().size());
        assertEquals(1, s.getCanonical().size());
        assertTrue(s.getCanonical().contains(canonical));
    }

    @Test
    @DisplayName("addPair with tWW added also to the canonical list")
    void tWWIsCanonical() {
        Pair p = new Pair(1, 8, "A", "U", BondType.LEONTIS_WESTHOF_tWW);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .addPair(p)
                .build();

        assertEquals(1, s.getCanonical().size());
    }

    @Test
    @DisplayName("addPair with non-canonical type do not add to canonical")
    void nonCanonicalPairNotInCanonicalList() {
        Pair nonCanonical = new Pair(0, 5, "G", "A", BondType.LEONTIS_WESTHOF_cWH);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .addPair(nonCanonical)
                .build();

        assertEquals(1, s.getPairs().size());
        assertTrue(s.getCanonical().isEmpty(),
                "A pair cWH should not go into the canonical list");
    }

    @Test
    @DisplayName("addPair with STACKING is not canonical")
    void stackingNotCanonical() {
        Pair stacking = new Pair(2, 3, "G", "U", BondType.STACKING);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .addPair(stacking)
                .build();

        assertTrue(s.getCanonical().isEmpty());
    }

    @Test
    @DisplayName("Pair mix: canonical and non-canonical are separated correctly")
    void mixedPairsSeparatedCorrectly() {
        Pair c1 = new Pair(0, 9, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair c2 = new Pair(1, 8, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair nc  = new Pair(3, 6, "A", "G", BondType.LEONTIS_WESTHOF_tHS);
        Pair st  = new Pair(4, 5, "U", "U", BondType.STACKING);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .setSequence("GGAUCAUCC")
                .addPair(c1).addPair(c2).addPair(nc).addPair(st)
                .build();

        assertEquals(4, s.getPairs().size());
        assertEquals(2, s.getCanonical().size());
        assertTrue(s.getCanonical().contains(c1));
        assertTrue(s.getCanonical().contains(c2));
        assertFalse(s.getCanonical().contains(nc));
        assertFalse(s.getCanonical().contains(st));
    }

    // ------------------------------------------------------------------ //
    //  Header info                                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("addHeaderInfo save the pair key-value")
    void headerInfoStored() {
        // Non esiste un getter pubblico per headerInfo: verifichiamo tramite toString
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .setSequence("ACGU")
                .addHeaderInfo("PDB ID", "1YMO")
                .addHeaderInfo("Chain ID", "A")
                .addPair(new Pair(0, 3, "A", "U", BondType.LEONTIS_WESTHOF_cWW))
                .build();

        String repr = s.toString();
        assertTrue(repr.contains("1YMO"),  "toString should contain the PDB ID");
        assertTrue(repr.contains("Chain ID"), "toString should contain the key Chain ID");
    }

    // ------------------------------------------------------------------ //
    //  Sequence                                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("setSequence overwrite the previous sequence")
    void setSequenceOverwrites() {
        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .setSequence("ACGU")
                .build();

        s.setSequence("GGGG");
        assertEquals("GGGG", s.getSequence());
    }

    // ------------------------------------------------------------------ //
    //  setPairs / setCanonical (bulk)                                      //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("setPairs replace the entire pair list")
    void setPairsReplacesList() {
        Pair p1 = new Pair(0, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair p2 = new Pair(1, 4, "A", "U", BondType.LEONTIS_WESTHOF_cWW);

        ExtendedRNASecondaryStructure s = new ExtendedRNASecondaryStructure.Builder()
                .setPairs(List.of(p1, p2))
                .build();

        assertEquals(2, s.getPairs().size());
    }
}