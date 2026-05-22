package it.unicam.cs.bdslab.rna2dunifier.models;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a base pair in an RNA secondary structure,
 * including the position of the paired nucleotides, the type of bond,
 * and optionally the nucleotides themselves.
 *
 * <p>Instances are immutable. Two pairs are considered equal regardless
 * of the order of the two residues (i.e., (pos1,pos2) is the same as (pos2,pos1)).
 *
 * @author Federico di Petta, Francesco Palozzi
 * @see BondType
 */
public final class Pair {

    private final int pos1;
    private final int pos2;
    private final BondType type;
    private final String nucleotide1;
    private final String nucleotide2;

    /**
     * Constructs a Pair with the specified positions and bond type.
     *
     * @param pos1 The position of the first nucleotide in the pair.
     * @param pos2 The position of the second nucleotide in the pair.
     * @param type The type of bond between the nucleotides, as defined in the BondType enum.
     */
    public Pair(int pos1, int pos2, BondType type) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.type = type;
        this.nucleotide1 = null;
        this.nucleotide2 = null;
    }

    /**
     * Constructs a Pair with the specified positions and an unknown bond type.
     *
     * @param pos1 The position of the first nucleotide in the pair.
     * @param pos2 The position of the second nucleotide in the pair.
     */
    public Pair(int pos1, int pos2) {
        this(pos1, pos2, BondType.UNKNOWN);
    }

    /**
     * Constructs a Pair with the specified positions, nucleotides, and an unknown bond type.
     *
     * @param pos1        The position of the first nucleotide in the pair.
     * @param pos2        The position of the second nucleotide in the pair.
     * @param nucleotide1 The nucleotide at the first position (e.g., "A", "U", "C", "G").
     * @param nucleotide2 The nucleotide at the second position (e.g., "A", "U", "C", "G").
     */
    public Pair(int pos1, int pos2, String nucleotide1, String nucleotide2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.type = BondType.UNKNOWN;
        this.nucleotide1 = nucleotide1;
        this.nucleotide2 = nucleotide2;
    }

    /**
     * Constructs a Pair with the specified positions, nucleotides, and bond type.
     *
     * @param pos1        The position of the first nucleotide in the pair.
     * @param pos2        The position of the second nucleotide in the pair.
     * @param nucleotide1 The nucleotide at the first position (e.g., "A", "U", "C", "G").
     * @param nucleotide2 The nucleotide at the second position (e.g., "A", "U", "C", "G").
     * @param type        The type of bond between the nucleotides, as defined in the BondType enum.
     */
    public Pair(int pos1, int pos2, String nucleotide1, String nucleotide2, BondType type) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.type = type;
        this.nucleotide1 = nucleotide1;
        this.nucleotide2 = nucleotide2;
    }

    /**
     * Returns the position of the first nucleotide.
     *
     * @return zero‑based index (or 1‑based depending on context)
     */
    public int getPos1() {
        return pos1;
    }

    /**
     * Returns the position of the second nucleotide.
     *
     * @return zero‑based index (or 1‑based depending on context)
     */
    public int getPos2() {
        return pos2;
    }

    /**
     * Returns the bond type.
     *
     * @return the {@link BondType} of this pair
     */
    public BondType getType() {
        return type;
    }

    /**
     * Returns the nucleotide at the first position.
     *
     * @return a single‑character string (e.g., "A", "U", "C", "G", "N"), may be {@code null}
     */
    public String getNucleotide1() {
        return nucleotide1;
    }

    /**
     * Returns the nucleotide at the second position.
     *
     * @return a single‑character string (e.g., "A", "U", "C", "G", "N"), may be {@code null}
     */
    public String getNucleotide2() {
        return nucleotide2;
    }

    /**
     * Returns a human‑readable string representation of the pair.
     *
     * @return string in the format "(type pos1:nuc1 pos2:nuc2)"
     */
    @Override
    public String toString() {
        return "(" + type + " " + pos1 + ":" + nucleotide1 + " " + pos2 + ":" + nucleotide2 + ")";
    }

    /**
     * Compares this pair with another object for equality.
     * Two pairs are considered equal if they represent the same unordered
     * residue pair (i.e., order of pos1/pos2 and nucleotide1/nucleotide2 does not matter)
     * and have the same bond type.
     *
     * @param o the object to compare with
     * @return {@code true} if the pairs are equivalent, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair pair = (Pair) o;

        // Positions must match as an unordered pair
        boolean positionsMatch = (pos1 == pair.pos1 && pos2 == pair.pos2) || (pos1 == pair.pos2 && pos2 == pair.pos1);

        // Nucleotides must match as an unordered pair, handling nulls
        return (
            positionsMatch &&
            type == pair.type &&
            nucleotidesEqualUnordered(nucleotide1, nucleotide2, pair.nucleotide1, pair.nucleotide2)
        );
    }

    private boolean nucleotidesEqualUnordered(String a1, String a2, String b1, String b2) {
        // Both null pairs
        if (a1 == null && a2 == null) {
            return b1 == null && b2 == null;
        }
        if (b1 == null && b2 == null) return false;

        // Compare unordered
        return (Objects.equals(a1, b1) && Objects.equals(a2, b2)) || (Objects.equals(a1, b2) && Objects.equals(a2, b1));
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     * The hash code is computed on the unordered pair of positions,
     * unordered nucleotides (lexicographically), and bond type.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int p1 = Math.min(pos1, pos2);
        int p2 = Math.max(pos1, pos2);

        String[] nucs = { nucleotide1, nucleotide2 };
        Arrays.sort(nucs, Comparator.nullsFirst(Comparator.naturalOrder()));
        // nucs[0] is the smaller (null if any null), nucs[1] the larger

        return Objects.hash(p1, p2, nucs[0], nucs[1], type);
    }

    /**
     * Builder class for {@link Pair}.
     * Provides a fluent interface to create {@code Pair} instances incrementally.
     */
    public static class Builder {

        private int pos1;
        private int pos2;
        private BondType type;
        private String nucleotide1;
        private String nucleotide2;

        /**
         * Sets the position of the first nucleotide.
         *
         * @param pos1 zero‑based index (or 1‑based depending on context)
         * @return this builder
         */
        public Builder setPos1(int pos1) {
            this.pos1 = pos1;
            return this;
        }

        /**
         * Sets the position of the second nucleotide.
         *
         * @param pos2 zero‑based index (or 1‑based depending on context)
         * @return this builder
         */
        public Builder setPos2(int pos2) {
            this.pos2 = pos2;
            return this;
        }

        /**
         * Sets the bond type.
         *
         * @param type the {@link BondType} for the pair
         * @return this builder
         */
        public Builder setType(BondType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the nucleotide at the first position.
         *
         * @param nucleotide1 a single‑character string (e.g., "A", "U", "C", "G", "N")
         * @return this builder
         */
        public Builder setNucleotide1(String nucleotide1) {
            this.nucleotide1 = nucleotide1;
            return this;
        }

        /**
         * Sets the nucleotide at the second position.
         *
         * @param nucleotide2 a single‑character string (e.g., "A", "U", "C", "G", "N")
         * @return this builder
         */
        public Builder setNucleotide2(String nucleotide2) {
            this.nucleotide2 = nucleotide2;
            return this;
        }

        /**
         * Builds the {@link Pair} instance.
         *
         * @return a new immutable {@code Pair}
         */
        public Pair build() {
            return new Pair(pos1, pos2, nucleotide1, nucleotide2, type);
        }
    }
}
