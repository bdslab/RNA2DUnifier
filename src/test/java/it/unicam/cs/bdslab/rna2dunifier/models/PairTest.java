package it.unicam.cs.bdslab.rna2dunifier.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link Pair}.
 */
@DisplayName("Pair")
class PairTest {

    // ------------------------------------------------------------------ //
    //  Constructors                                                         //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Constructors with positions and BondType set correctly the fields")
    void constructorWithBondType() {
        Pair p = new Pair(1, 10, BondType.LEONTIS_WESTHOF_cWW);
        assertEquals(1,  p.getPos1());
        assertEquals(10, p.getPos2());
        assertEquals(BondType.LEONTIS_WESTHOF_cWW, p.getType());
        assertNull(p.getNucleotide1());
        assertNull(p.getNucleotide2());
    }

    @Test
    @DisplayName("Constructor with only positions use BondType.UNKNOWN")
    void constructorDefaultBondType() {
        Pair p = new Pair(2, 5);
        assertEquals(BondType.UNKNOWN, p.getType());
    }

    @Test
    @DisplayName("Constructor with nucleotides set correctly the bases and use UNKNOWN")
    void constructorWithNucleotides() {
        Pair p = new Pair(0, 9, "G", "C");
        assertEquals("G", p.getNucleotide1());
        assertEquals("C", p.getNucleotide2());
        assertEquals(BondType.UNKNOWN, p.getType());
    }

    @Test
    @DisplayName("Complete Constructor")
    void constructorFull() {
        Pair p = new Pair(3, 14, "A", "U", BondType.LEONTIS_WESTHOF_tWW);
        assertEquals(3,  p.getPos1());
        assertEquals(14, p.getPos2());
        assertEquals("A", p.getNucleotide1());
        assertEquals("U", p.getNucleotide2());
        assertEquals(BondType.LEONTIS_WESTHOF_tWW, p.getType());
    }

    // ------------------------------------------------------------------ //
    //  equals                                                            //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("equals – two identical pairs are equal")
    void equalsIdentical() {
        Pair a = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair b = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals – positions order doesn't count")
    void equalsSymmetric() {
        Pair a = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair b = new Pair(5, 1, "C", "G", BondType.LEONTIS_WESTHOF_cWW);
        assertEquals(a, b, "Pair(1,5,G,C) should be equal to Pair(5,1,C,G)");
        assertEquals(b, a, "equals should be symmetric");
    }

    @Test
    @DisplayName("equals – BondType different make pairs not equals")
    void equalsDifferentBondType() {
        Pair a = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair b = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_tWW);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals – different nucleotides make pairs not equals")
    void equalsDifferentNucleotides() {
        Pair a = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair b = new Pair(1, 5, "A", "U", BondType.LEONTIS_WESTHOF_cWW);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals – null return false")
    void equalsNull() {
        Pair a = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        assertNotEquals(null, a);
    }

    @Test
    @DisplayName("equals – different object type return false")
    void equalsWrongType() {
        Pair a = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        assertNotEquals("string", a);
    }

    // ------------------------------------------------------------------ //
    //  hashCode                                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("hashCode – pair equals have the same hashcode")
    void hashCodeConsistentWithEquals() {
        Pair a = new Pair(1, 5, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        Pair b = new Pair(5, 1, "C", "G", BondType.LEONTIS_WESTHOF_cWW);
        assertEquals(a.hashCode(), b.hashCode(),
                "Pair symmetrics should have the same hashCode");
    }

    // ------------------------------------------------------------------ //
    //  Builder                                                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Builder build a pair correctly")
    void builderFull() {
        Pair p = new Pair.Builder()
                .setPos1(2)
                .setPos2(8)
                .setNucleotide1("A")
                .setNucleotide2("U")
                .setType(BondType.LEONTIS_WESTHOF_cHS)
                .build();

        assertEquals(2, p.getPos1());
        assertEquals(8, p.getPos2());
        assertEquals("A", p.getNucleotide1());
        assertEquals("U", p.getNucleotide2());
        assertEquals(BondType.LEONTIS_WESTHOF_cHS, p.getType());
    }

    @Test
    @DisplayName("Builder produce a Pair equal to the constructor")
    void builderEquivalentToConstructor() {
        Pair fromConstructor = new Pair(3, 11, "G", "C", BondType.LEONTIS_WESTHOF_tWH);
        Pair fromBuilder = new Pair.Builder()
                .setPos1(3)
                .setPos2(11)
                .setNucleotide1("G")
                .setNucleotide2("C")
                .setType(BondType.LEONTIS_WESTHOF_tWH)
                .build();
        assertEquals(fromConstructor, fromBuilder);
    }

    // ------------------------------------------------------------------ //
    //  toString                                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("toString contains the bond type and the positions")
    void toStringContainsTypeAndPositions() {
        Pair p = new Pair(0, 6, "G", "C", BondType.LEONTIS_WESTHOF_cWW);
        String s = p.toString();
        assertTrue(s.contains("cWW"), "toString should contain BondType info");
        assertTrue(s.contains("0"),   "toString should contain pos1");
        assertTrue(s.contains("6"),   "toString should contain pos2");
    }
}