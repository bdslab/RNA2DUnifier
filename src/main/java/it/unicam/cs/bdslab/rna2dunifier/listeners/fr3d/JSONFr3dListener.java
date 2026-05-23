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
        logger.debug("Started parsing FR3D JSON file");
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
     * If inside the "annotations" array, creates a new {@link Pair.Builder}.
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
     * Processes various member names:
     * <ul>
     *   <li>"pdb_id" – stores the PDB ID as header info</li>
     *   <li>"chain_id" – stores the chain ID as header info</li>
     *   <li>"annotations" – enters the annotations section and builds position mapping</li>
     *   <li>"modified" – collects sequence IDs from modified residues</li>
     *   <li>Inside annotations – calls {@link #buildPair(String, JSONParser.MemberContext)}</li>
     * </ul>
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
     * Pops the member name from the stack and, if the stack becomes empty,
     * exits the annotations mode.
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

    private static String stripQuotes(String s) {
        return s.replaceAll("^\"|\"$", "");
    }

    private String getStringValue(JSONParser.ValueContext valueCtx) {
        if (valueCtx.STRING() != null) {
            return stripQuotes(valueCtx.STRING().getText());
        }
        if (valueCtx.NUMBER() != null) {
            return valueCtx.NUMBER().getText();
        }
        // Boolean, null, object, array – not used for our fields
        return null;
    }

    private void collectPositionsFromAnnotations(JSONParser.MemberContext ctx) {
        // Walk the annotations array and collect all seq_id1, seq_id2, seq_id
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

    private void buildPositionMapping() {
        List<Integer> sorted = new ArrayList<>(seenPositions);
        Collections.sort(sorted);
        for (int i = 0; i < sorted.size(); i++) {
            positionMap.put(sorted.get(i), i);
        }
        logger.debug("Built position map with {} entries", positionMap.size());
    }

    // ----------------------------------------------------------------------
    // Helper class for pair management
    // ----------------------------------------------------------------------
    private static final class PairBuilderHelper {

        private final Map<Integer, Integer> positionMap;
        private Pair.Builder currentBuilder;

        PairBuilderHelper(Map<Integer, Integer> positionMap) {
            this.positionMap = positionMap;
        }

        void newPair() {
            currentBuilder = new Pair.Builder();
        }

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

        private String sanitizeNucleotide(String nt) {
            if (nt.length() > 1) {
                logger.warn("Uncommon residue '{}' – keeping as is (may be non‑standard)", nt);
            }
            return nt;
        }

        Pair build() {
            if (currentBuilder == null) return null;
            Pair p = currentBuilder.build();
            // Validate that both positions are set (not default 0 when they should be positive)
            if (p.getPos1() < 0 || p.getPos2() < 0) {
                logger.warn("Skipping pair with invalid indices: ({},{})", p.getPos1(), p.getPos2());
                return null;
            }
            return p;
        }
    }
}
