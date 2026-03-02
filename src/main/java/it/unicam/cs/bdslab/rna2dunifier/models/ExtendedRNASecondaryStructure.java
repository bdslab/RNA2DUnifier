package it.unicam.cs.bdslab.rna2dunifier.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedRNASecondaryStructure {
    private String sequence;
    private List<Pair> pairs;
    private List<Pair> canonical;
    private Map<String,String> headerInfo;

    /**
     * Base construcutor, monstly for testing purposes.
     */
    public ExtendedRNASecondaryStructure(String sequence, List<Pair> pairs) {
        this.sequence = sequence;
        this.pairs = pairs;
    }

    private ExtendedRNASecondaryStructure(Builder builder) {
        this.sequence = builder.sequence;
        this.pairs = builder.pairs;
        this.canonical = builder.canonical;
        this.headerInfo = builder.headerInfo;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<Pair> getPairs() {
        return pairs;
    }

    public List<Pair> getCanonical() {
        return canonical;
    }

    public static class Builder {
        private String sequence = "";
        private List<Pair> pairs = new ArrayList<>();
        private List<Pair> canonical = new ArrayList<>();
        private Map<String,String> headerInfo = new HashMap<>();

        public Builder setSequence(String sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder setPairs(List<Pair> pairs) {
            this.pairs = pairs;
            return this;
        }

        public Builder setCanonical(List<Pair> canonical) {
            this.canonical = canonical;
            return this;
        }

        public Builder addPair(Pair pair) {
            this.pairs.add(pair);
            if (pair.getType().isCanonical()) {
                this.canonical.add(pair);
            }
            return this;
        }

        public Builder addHeaderInfo(String key, String value) {
            this.headerInfo.put(key, value);
            return this;
        }

        public ExtendedRNASecondaryStructure build() {
            return new ExtendedRNASecondaryStructure(this);
        }
    }
}
