package it.unicam.cs.bdslab.rna2dunifier.models;

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
public class Pair {

    private final int pos1;
    private final int pos2;
    private final BondType type;
    private String nucleotide1;
    private String nucleotide2;

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
        this(pos1, pos2);
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
        this(pos1, pos2, type);
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
        if (o == null) return false;
        if (!(o instanceof Pair pair)) return false;

        if (pos1 == pair.getPos1() &&
                pos2 == pair.getPos2() &&
                nucleotide1.equals(pair.getNucleotide1()) &&
                nucleotide2.equals(pair.getNucleotide2()) &&
                type == pair.getType()) {
            return true;
        }

        return pos1 == pair.getPos2() &&
                pos2 == pair.getPos1() &&
                nucleotide1.equals(pair.getNucleotide2()) &&
                nucleotide2.equals(pair.getNucleotide1()) &&
                type == pair.getType();
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

        // Canonical order for nucleotides (e.g., lexicographic)
        String n1, n2;
        if (nucleotide1.compareTo(nucleotide2) <= 0) {
            n1 = nucleotide1;
            n2 = nucleotide2;
        } else {
            n1 = nucleotide2;
            n2 = nucleotide1;
        }

        return Objects.hash(p1, p2, n1, n2, type);
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