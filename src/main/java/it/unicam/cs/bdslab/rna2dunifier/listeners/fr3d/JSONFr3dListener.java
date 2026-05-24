package it.unicam.cs.bdslab.rna2dunifier.listeners.fr3d;

import it.unicam.cs.bdslab.JSON.JSONBaseListener;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom ANTLR listener for parsing FR3D JSON output files.
 *
 * <p>This listener processes JSON grammar parse events (from FR3D output)
 * to build an {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Extracting PDB ID and chain ID as header information</li>
 *   <li>Collecting residue sequence IDs from the "modified" and "annotations" arrays</li>
 *   <li>Mapping original sequence IDs to zero‑based indices</li>
 *   <li>Building base‑pair objects from the "annotations" array</li>
 * </ul>
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class JSONFr3dListener extends JSONBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(JSONFr3dListener.class);

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** Set of original sequence IDs extracted from the JSON. */
    private final Set<Integer> seenPositions = new HashSet<>();

    /** Maps original sequence IDs to zero‑based indices. */
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    /** Helper for building pairs inside the "annotations" array. */
    private PairBuilderHelper pairHelper;

    /** Flag indicating whether we are inside the "annotations" array. */
    private boolean inAnnotations = false;

    /**
     * Returns the parsed RNA secondary structure.
     *
     * @return the built {@link ExtendedRNASecondaryStructure}
     */
    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    /**
     * Called when entering the root {@code json} rule.
     * Initialises the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterJson(JSONParser.JsonContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
        if (logger.isDebugEnabled()) logger.debug("Started parsing FR3D JSON file");
    }

    /**
     * Called when exiting the root {@code json} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitJson(JSONParser.JsonContext ctx) {
        this.structure = structureBuilder.build();
        logger.info(
            "Finished FR3D parsing: sequence length={}, pairs={}",
            structure.getSequence() != null ? structure.getSequence().length() : 0,
            structure.getPairs().size()
        );
    }

    /**
     * Called when entering an {@code object} rule.
     * If inside the "annotations" array, creates a new {@link Pair.Builder} via the helper.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterObject(JSONParser.ObjectContext ctx) {
        if (inAnnotations) {
            pairHelper = new PairBuilderHelper(positionMap);
            pairHelper.newPair();
        }
    }

    /**
     * Called when exiting an {@code object} rule.
     * If inside the "annotations" array, adds the completed pair to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitObject(JSONParser.ObjectContext ctx) {
        if (inAnnotations && pairHelper != null) {
            Pair p = pairHelper.build();
            if (p != null) {
                structureBuilder.addPair(p);
            }
        }
        pairHelper = null;
    }

    /**
     * Called when entering a {@code member} rule (a key‑value pair in a JSON object).
     * Processes top‑level fields ({@code pdb_id}, {@code chain_id}, {@code modified})
     * and enters the {@code annotations} array, collecting positions and delegating
     * pair fields to the helper.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterMember(JSONParser.MemberContext ctx) {
        String key = stripQuotes(ctx.STRING().getText());

        if (key.equals("annotations")) {
            inAnnotations = true;
            // First pass: collect all seq_id values from annotations array
            collectPositionsFromAnnotations(ctx);
            return;
        }

        // Handle top-level fields before or after annotations
        if (!inAnnotations) {
            switch (key) {
                case "pdb_id":
                    structureBuilder.addHeaderInfo("PDB ID", getStringValue(ctx.value()));
                    break;
                case "chain_id":
                    structureBuilder.addHeaderInfo("Chain ID", getStringValue(ctx.value()));
                    break;
                case "modified":
                    collectPositionsFromModified(ctx);
                    break;
            }
        } else {
            // Inside annotations, pass key‑value to the helper
            if (pairHelper != null) {
                String val = getStringValue(ctx.value());
                if (val != null) {
                    pairHelper.setField(key, val);
                }
            }
        }
    }

    /**
     * Called when exiting a {@code member} rule.
     * If the member is {@code annotations}, exits the annotations mode.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitMember(JSONParser.MemberContext ctx) {
        String key = stripQuotes(ctx.STRING().getText());
        if (key.equals("annotations")) {
            inAnnotations = false;
            pairHelper = null;
        }
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    /** Removes leading and trailing double quotes from a string. */
    private static String stripQuotes(String s) {
        return s.replaceAll("^\"|\"$", "");
    }

    /**
     * Extracts the string value from a JSON value context.
     * Handles both quoted strings and numbers.
     *
     * @param valueCtx the value context
     * @return the extracted string, or {@code null} if not a string or number
     */
    private String getStringValue(JSONParser.ValueContext valueCtx) {
        if (valueCtx.STRING() != null) {
            return stripQuotes(valueCtx.STRING().getText());
        }
        if (valueCtx.NUMBER() != null) {
            return valueCtx.NUMBER().getText();
        }
        return null;
    }

    /**
     * Collects all sequence ID values (seq_id, seq_id1, seq_id2) from the "annotations" array
     * and stores them in {@code seenPositions}. Then builds the position mapping.
     *
     * @param ctx the member context of the "annotations" key
     */
    private void collectPositionsFromAnnotations(JSONParser.MemberContext ctx) {
        JSONParser.ArrayContext array = ctx.value().array();
        if (array == null) return;

        for (JSONParser.ValueContext val : array.value()) {
            JSONParser.ObjectContext obj = val.object();
            if (obj == null) continue;
            for (JSONParser.MemberContext member : obj.member()) {
                String key = stripQuotes(member.STRING().getText());
                if (key.startsWith("seq_id")) {
                    String strVal = getStringValue(member.value());
                    if (strVal != null) {
                        try {
                            seenPositions.add(Integer.parseInt(strVal));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid seq_id value: {}", strVal);
                        }
                    }
                }
            }
        }

        buildPositionMapping();
    }

    /**
     * Collects sequence ID values from the "modified" array (where residues are listed as objects
     * with a {@code seq_id} field). Adds them to {@code seenPositions}.
     *
     * @param ctx the member context of the "modified" key
     */
    private void collectPositionsFromModified(JSONParser.MemberContext ctx) {
        JSONParser.ArrayContext array = ctx.value().array();
        if (array == null) return;

        for (JSONParser.ValueContext val : array.value()) {
            JSONParser.ObjectContext obj = val.object();
            if (obj == null) continue;
            for (JSONParser.MemberContext member : obj.member()) {
                String key = stripQuotes(member.STRING().getText());
                if ("seq_id".equals(key)) {
                    String strVal = getStringValue(member.value());
                    if (strVal != null) {
                        try {
                            seenPositions.add(Integer.parseInt(strVal));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid seq_id in modified: {}", strVal);
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds the mapping from original sequence IDs to zero‑based indices.
     * Sorts the collected IDs and assigns sequential indices.
     */
    private void buildPositionMapping() {
        List<Integer> sorted = new ArrayList<>(seenPositions);
        Collections.sort(sorted);
        for (int i = 0; i < sorted.size(); i++) {
            positionMap.put(sorted.get(i), i);
        }
        if (logger.isDebugEnabled()) logger.debug("Built position map with {} entries", positionMap.size());
    }

    // ----------------------------------------------------------------------
    // Helper class for pair management
    // ----------------------------------------------------------------------

    /**
     * Internal helper that manages the construction of a single base pair
     * from the JSON fields inside the "annotations" array.
     * <p>
     * This class is not thread‑safe, but each instance is used exclusively
     * for one pair and discarded afterwards.
     */
    private static final class PairBuilderHelper {

        private final Map<Integer, Integer> positionMap;
        private Pair.Builder currentBuilder;

        /**
         * Creates a new helper with the given position mapping.
         *
         * @param positionMap mapping from original PDB sequence IDs to zero‑based indices
         */
        PairBuilderHelper(Map<Integer, Integer> positionMap) {
            this.positionMap = positionMap;
        }

        /** Initialises a new {@link Pair.Builder} for the next pair. */
        void newPair() {
            currentBuilder = new Pair.Builder();
        }

        /**
         * Sets a field of the current pair.
         * Recognised keys: "seq_id1", "seq_id2", "nt1", "nt2", "bp".
         *
         * @param key   the field name
         * @param value the field value as a string
         */
        void setField(String key, String value) {
            if (currentBuilder == null) return;

            switch (key) {
                case "seq_id1":
                    setPosition(value, currentBuilder::setPos1);
                    break;
                case "seq_id2":
                    setPosition(value, currentBuilder::setPos2);
                    break;
                case "nt1":
                    currentBuilder.setNucleotide1(sanitizeNucleotide(value));
                    break;
                case "nt2":
                    currentBuilder.setNucleotide2(sanitizeNucleotide(value));
                    break;
                case "bp":
                    BondType type = BondType.fromString(value);
                    if (type == BondType.UNKNOWN) {
                        logger.warn("Unknown bond type '{}'", value);
                    }
                    currentBuilder.setType(type);
                    break;
            }
        }

        /**
         * Converts a string representation of a sequence ID to a zero‑based index
         * and sets it via the provided setter.
         *
         * @param value  the string containing the original ID
         * @param setter consumer that accepts the zero‑based index
         */
        private void setPosition(String value, java.util.function.Consumer<Integer> setter) {
            try {
                int orig = Integer.parseInt(value);
                Integer zero = positionMap.get(orig);
                if (zero == null) {
                    logger.error("Position {} not found in map, pair will be incomplete", orig);
                    currentBuilder = null;
                } else {
                    setter.accept(zero);
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid position value '{}'", value);
                currentBuilder = null;
            }
        }

        /**
         * Sanitises a nucleotide string. Warns if it has length > 1 (uncommon residue)
         * but leaves it unchanged.
         *
         * @param nt the nucleotide string
         * @return the same string
         */
        private String sanitizeNucleotide(String nt) {
            if (nt.length() > 1) {
                logger.warn("Uncommon residue '{}' – transforming to 'N'", nt);
                return "N";
            }
            return nt;
        }

        /**
         * Builds the completed pair, or returns {@code null} if the pair is invalid
         * (builder missing or indices out of range).
         *
         * @return the built {@link Pair}, or {@code null}
         */
        Pair build() {
            if (currentBuilder == null) return null;
            Pair p = currentBuilder.build();
            if (p.getPos1() < 0 || p.getPos2() < 0) {
                logger.warn("Skipping pair with invalid indices: ({},{})", p.getPos1(), p.getPos2());
                return null;
            }
            return p;
        }
    }
}
