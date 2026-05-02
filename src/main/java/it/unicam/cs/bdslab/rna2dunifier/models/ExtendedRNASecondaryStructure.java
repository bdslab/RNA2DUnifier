package it.unicam.cs.bdslab.rna2dunifier.models;

import java.util.*;

/**
 * Represents an extended RNA secondary structure, including the nucleotide sequence,
 * a list of all base pairs (including stacking and non‑canonical interactions),
 * a separate list of canonical (Watson‑Crick/Watson‑Crick) pairs, and optional header
 * information (e.g., PDB ID, chain ID, strand name).
 *
 * <p>This class is immutable after construction; a {@link Builder} is provided
 * for convenient creation and incremental addition of pairs.
 *
 * @author Federico di Petta, Francesco Palozzi
 * @see Pair
 * @see BondType
 */
public class ExtendedRNASecondaryStructure {

    /** The RNA sequence as a string of nucleotides (A, C, G, U, possibly N). */
    private String sequence;

    /** List of all base pairs (including stacking and non‑canonical). */
    private List<Pair> pairs;

    /** Subset of {@link #pairs} that are canonical (cWW or tWW). */
    private List<Pair> canonical;

    /** Optional metadata (e.g., "PDB ID", "Chain ID", "strand_name"). */
    private Map<String, String> headerInfo;

    /**
     * Basic constructor, mostly for testing purposes.
     *
     * @param sequence the nucleotide sequence
     * @param pairs    the list of pairs (may include stacking and non‑canonical)
     * @deprecated Use the {@link Builder} instead for new code.
     */
    @Deprecated
    public ExtendedRNASecondaryStructure(String sequence, List<Pair> pairs) {
        this.sequence = sequence;
        this.pairs = pairs;
    }

    /**
     * Private constructor used by the {@link Builder}.
     *
     * @param builder the builder containing all structure data
     */
    private ExtendedRNASecondaryStructure(Builder builder) {
        this.sequence = builder.sequence;
        this.pairs = builder.pairs;
        this.canonical = builder.canonical;
        this.headerInfo = builder.headerInfo;
    }

    /**
     * Returns the nucleotide sequence.
     *
     * @return the sequence string
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Sets the nucleotide sequence.
     *
     * @param sequence the new sequence string
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the list of all base pairs (including stacking and non‑canonical).
     *
     * @return an unmodifiable view is recommended, but currently returns the internal list
     */
    public List<Pair> getPairs() {
        return pairs;
    }

    /**
     * Returns the list of canonical base pairs (cWW or tWW).
     *
     * @return the canonical pairs list
     */
    public List<Pair> getCanonical() {
        return canonical;
    }

    /**
     * Return the header infos of the structure
     * @return the header infos
     */
    public Map<String, String> getHeaderInfo() {
        return Collections.unmodifiableMap(headerInfo);
    }

    /**
     * Returns a human‑readable string representation of the structure,
     * including header info, sequence, canonical pairs, and all pairs.
     *
     * @return formatted string with all structure data
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        // header info
        result.append("Header Info: \n");
        headerInfo.forEach((key, value) ->
                result.append(key).append(": ").append(value).append("\n")
        );

        // sequence
        result.append("Sequence: \n").append(sequence).append("\n");

        // canonical pairs
        result.append("Canonical Pairs: \n");
        canonical.forEach(c ->
                result.append(c.toString()).append("\n")
        );

        // pairs
        result.append("Pairs: \n");
        pairs.forEach(p ->
                result.append(p.toString()).append("\n")
        );

        return result.toString();
    }

    /**
     * Builder class for {@link ExtendedRNASecondaryStructure}.
     * Provides a fluent interface to construct the immutable structure incrementally.
     */
    public static class Builder {
        private String sequence = "";
        private List<Pair> pairs = new ArrayList<>();
        private List<Pair> canonical = new ArrayList<>();
        private Map<String, String> headerInfo = new HashMap<>();

        /**
         * Sets the nucleotide sequence.
         *
         * @param sequence the RNA sequence
         * @return this builder
         */
        public Builder setSequence(String sequence) {
            this.sequence = sequence;
            return this;
        }

        /**
         * Replaces the current pair list with the given list.
         * <p>Note: This does <strong>not</strong> automatically update the canonical list.
         * For incremental addition, prefer {@link #addPair(Pair)}.
         *
         * @param pairs the new list of pairs
         * @return this builder
         */
        public Builder setPairs(List<Pair> pairs) {
            this.pairs = pairs;
            return this;
        }

        /**
         * Replaces the current canonical pair list with the given list.
         *
         * @param canonical the new list of canonical pairs
         * @return this builder
         */
        public Builder setCanonical(List<Pair> canonical) {
            this.canonical = canonical;
            return this;
        }

        /**
         * Adds a single pair to the structure. If the pair's type is canonical
         * (cWW or tWW), it is also added to the canonical list.
         *
         * @param pair the pair to add
         * @return this builder
         */
        public Builder addPair(Pair pair) {
            this.pairs.add(pair);
            if (pair.getType().isCanonical()) {
                this.canonical.add(pair);
            }
            return this;
        }

        /**
         * Adds a key‑value pair to the header information (metadata).
         *
         * @param key   the header key (e.g., "PDB ID")
         * @param value the corresponding value
         * @return this builder
         */
        public Builder addHeaderInfo(String key, String value) {
            this.headerInfo.put(key, value);
            return this;
        }

        /**
         * Builds the final {@link ExtendedRNASecondaryStructure} instance.
         *
         * @return an immutable structure (the builder can be discarded afterwards)
         */
        public ExtendedRNASecondaryStructure build() {
            return new ExtendedRNASecondaryStructure(this);
        }
    }
}