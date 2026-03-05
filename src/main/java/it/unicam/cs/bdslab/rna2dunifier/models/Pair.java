package it.unicam.cs.bdslab.rna2dunifier.models;

/**
 * Represents a base pair in an RNA secondary structure,
 * including the position of the paired nucleotides, the type of bond, and optionally the nucleotides themselves.
 */
public class Pair {
    
    private int pos1;
    private int pos2;
    private BondType type;
    private String nucleotide1;
    private String nucleotide2;

    /**
     * Constructs a Pair with the specified positions and bond type.
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
     * @param pos1 The position of the first nucleotide in the pair.
     * @param pos2 The position of the second nucleotide in the pair.
     */
    public Pair(int pos1, int pos2) {
        this(pos1, pos2, BondType.UNKNOWN);
    }

    /**
     * Constructs a Pair with the specified positions, nucleotides, and an unknown bond type.
     * @param pos1 The position of the first nucleotide in the pair.
     * @param pos2 The position of the second nucleotide in the pair.
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
     * @param pos1 The position of the first nucleotide in the pair.
     * @param pos2 The position of the second nucleotide in the pair.
     * @param nucleotide1 The nucleotide at the first position (e.g., "A", "U", "C", "G").
     * @param nucleotide2 The nucleotide at the second position (e.g., "A", "U", "C", "G").
     * @param type The type of bond between the nucleotides, as defined in the BondType enum.
     */
    public Pair(int pos1, int pos2, String nucleotide1, String nucleotide2, BondType type) {
        this(pos1, pos2, type);
        this.nucleotide1 = nucleotide1;
        this.nucleotide2 = nucleotide2;
    }

    public int getPos1() {
        return pos1;
    }

    public int getPos2() {
        return pos2;
    }

    public BondType getType() {
        return type;
    }

    public String getNucleotide1() {
        return nucleotide1;
    }

    public String getNucleotide2() {
        return nucleotide2;
    }

    @Override
    public String toString() {
        return "(" + type + " " + pos1 + ":" + nucleotide1 + " " + pos2 + ":" + nucleotide2 + ")";
    }

    public static class Builder {
        private int pos1;
        private int pos2;
        private BondType type;
        private String nucleotide1;
        private String nucleotide2;

        public Builder setPos1(int pos1) {
            this.pos1 = pos1;
            return this;
        }

        public Builder setPos2(int pos2) {
            this.pos2 = pos2;
            return this;
        }

        public Builder setType(BondType type) {
            this.type = type;
            return this;
        }

        public Builder setNucleotide1(String nucleotide1) {
            this.nucleotide1 = nucleotide1;
            return this;
        }

        public Builder setNucleotide2(String nucleotide2) {
            this.nucleotide2 = nucleotide2;
            return this;
        }

        public Pair build() {
            return new Pair(pos1, pos2, nucleotide1, nucleotide2, type);
        }
    }
}
