package it.unicam.cs.bdslab.rna2dunifier.listeners.x3dna;

import it.unicam.cs.bdslab.JSON.JSONBaseListener;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom ANTLR listener for parsing x3dna JSON output files.
 *
 * <p>This listener processes JSON grammar parse events (from x3dna output)
 * to build an {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Extracting base‑pair information from the "pairs" array</li>
 *   <li>Parsing nucleotide identifiers (e.g., "A1" → nucleotide 'A', position 1)</li>
 *   <li>Converting x3dna bond type annotations (e.g., "cWW", "tSH") into internal {@link BondType}</li>
 * </ul>
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class JSONX3dnaListener extends JSONBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(JSONX3dnaListener.class);

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** Builder for the current base pair being processed. */
    private Pair.Builder currentPairBuilder;

    /** position map to normalize nucleotide positions */
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    /** Flag indicating whether we are inside the "pairs" array. */
    private boolean inPairs = false;

    /** Helper class for parsing residue identifiers. */
    private ResidueParser residueParser;

    private int depth = 0;

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
        structureBuilder = new ExtendedRNASecondaryStructure.Builder();
        residueParser = new ResidueParser(positionMap);
        logger.debug("Started parsing x3dna JSON file");
    }

    /**
     * Called when exiting the root {@code json} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitJson(JSONParser.JsonContext ctx) {
        structure = structureBuilder.build();
        logger.info("Finished x3dna parsing: pairs={}", structure.getPairs().size());
    }

    /**
     * Called when entering an {@code object} rule.
     * If inside the "pairs" array, creates a new {@link Pair.Builder}.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterObject(JSONParser.ObjectContext ctx) {
        if (inPairs) {
            currentPairBuilder = new Pair.Builder();
        }
    }

    /**
     * Called when exiting an {@code object} rule.
     * If inside the "pairs" array, adds the completed pair to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitObject(JSONParser.ObjectContext ctx) {
        if (inPairs && currentPairBuilder != null) {
            Pair p = currentPairBuilder.build();
            if (p.getPos1() >= 0 && p.getPos2() >= 0) {
                structureBuilder.addPair(p);
            } else {
                logger.warn("Skipping invalid pair: ({},{})", p.getPos1(), p.getPos2());
            }
        }
        currentPairBuilder = null;
    }

    /**
     * Called when entering a {@code member} rule (a key‑value pair in a JSON object).
     * Processes member names:
     * <ul>
     *   <li>If the member name is "pairs", sets the {@code inPairs} flag to true</li>
     *   <li>Otherwise, if inside "pairs", calls {@link #buildPair(String, JSONParser.MemberContext)}</li>
     * </ul>
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterMember(JSONParser.MemberContext ctx) {
        String key = stripQuotes(ctx.STRING().getText());
        depth++;

        if (depth == 1 && key.equals("pairs")) {
            inPairs = true;
            buildPositionMap(ctx);
        } else if (inPairs && currentPairBuilder != null) {
            String val = getStringValue(ctx.value());
            if (val == null) return;

            switch (key) {
                case "nt1":
                    residueParser.setResidue(currentPairBuilder, val, true);
                    break;
                case "nt2":
                    residueParser.setResidue(currentPairBuilder, val, false);
                    break;
                case "LW":
                    currentPairBuilder.setType(BondType.fromString(val));
                    break;
            }
        }
    }

    /**
     * Called when exiting a {@code member} rule.
     * Pops the member name from the stack and, if the stack becomes empty,
     * exits the "pairs" and "nts" modes.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitMember(JSONParser.MemberContext ctx) {
        depth--;
        String key = stripQuotes(ctx.STRING().getText());
        logger.debug("Uscita pairs");
        if (key.equals("pairs")) {
            inPairs = false;
        }
    }

    private void buildPositionMap(JSONParser.MemberContext ctx) {
        Set<Integer> positions = new HashSet<>();
        JSONParser.ArrayContext array = ctx.value().array();
        if (array == null) return;

        for (JSONParser.ValueContext val : array.value()) {
            JSONParser.ObjectContext obj = val.object();
            if (obj == null) continue;
            for (JSONParser.MemberContext member : obj.member()) {
                String key = stripQuotes(member.STRING().getText());
                if (key.equals("nt1") || key.equals("nt2")) {
                    String full = getStringValue(member.value());
                    if (full != null) {
                        Integer pos = residueParser.extractPosition(full);
                        if (pos != null) positions.add(pos);
                    }
                }
            }
        }

        List<Integer> sorted = new ArrayList<>(positions);
        Collections.sort(sorted);
        positionMap.clear();
        for (int i = 0; i < sorted.size(); i++) {
            positionMap.put(sorted.get(i), i);
        }
        logger.debug("Position map built with {} entries", positionMap.size());
    }

    // ----------------------------------------------------------------------
    // Utility methods
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
        return null;
    }

    // ----------------------------------------------------------------------
    // Helper class for residue parsing
    // ----------------------------------------------------------------------

    /**
     * Helper class for parsing x3dna residue identifiers.
     * Handles formats: "G2", "GTP1", "A23/76"
     */
    private static final class ResidueParser {

        private static final Pattern BASE_PATTERN = Pattern.compile("^([A-Z]+)");
        private static final Pattern POSITION_PATTERN = Pattern.compile("^[A-Z]+([0-9]+)");

        private final Map<Integer, Integer> positionMap;

        ResidueParser(Map<Integer, Integer> positionMap) {
            this.positionMap = positionMap;
        }

        /**
         * Extracts and sets residue information (position and nucleotide) into a Pair.Builder.
         *
         * @param builder the Pair.Builder to update
         * @param fullIdentifier the full string (e.g., "A.G2")
         * @param isFirst true for nt1, false for nt2
         */
        void setResidue(Pair.Builder builder, String fullIdentifier, boolean isFirst) {
            String residuePart = extractIdentifier(fullIdentifier);
            if (residuePart == null) return;

            String[] parsed = parse(residuePart);
            if (parsed == null) return;

            String base = parsed[0];
            int pdbPos;
            try {
                pdbPos = Integer.parseInt(parsed[1]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid position in '{}'", residuePart);
                return;
            }

            Integer idx = positionMap.get(pdbPos);
            if (idx == null) {
                logger.warn("Position {} not in map", pdbPos);
                return;
            }

            if (isFirst) {
                builder.setPos1(idx).setNucleotide1(base);
            } else {
                builder.setPos2(idx).setNucleotide2(base);
            }
        }

        /**
         * Extracts just the position number from a full identifier.
         *
         * @param fullIdentifier the full string (e.g., "A.G2")
         * @return the original PDB position, or null if extraction fails
         */
        Integer extractPosition(String fullIdentifier) {
            String residuePart = extractIdentifier(fullIdentifier);
            if (residuePart == null) return null;
            String[] parsed = parse(residuePart);
            if (parsed == null) return null;
            try {
                return Integer.parseInt(parsed[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /**
         * Extracts the residue part (after the dot) from a full identifier.
         */
        private String extractIdentifier(String full) {
            if (full == null || full.isEmpty()) return null;
            String[] parts = full.split("\\.");
            if (parts.length < 2) {
                logger.warn("No dot in '{}', using as is (may cause issues)", full);
                return full;
            }
            // In case of multiple dots, take everything after the first dot
            // (e.g., "A.B.C2" -> "B.C2" – not expected in x3dna, but safe)
            return full.substring(full.indexOf('.') + 1);
        }

        /**
         * Parses a residue string into [base, position].
         * Handles: "G2", "GTP1", "A23/76"
         */
        private String[] parse(String residue) {
            String basePart;
            String posStr;

            String[] slashParts = residue.split("/");
            if (slashParts.length == 2) {
                basePart = slashParts[0];
                posStr = slashParts[1];
            } else {
                basePart = residue;
                posStr = null;
            }

            // Extract the base: first character of the alphabetic prefix
            Matcher baseMatcher = BASE_PATTERN.matcher(basePart);
            if (!baseMatcher.find()) {
                logger.warn("No alphabetic base found in '{}'", basePart);
                return null;
            }
            String rawBase = baseMatcher.group(1);
            String base;
            if (rawBase.length() == 1) {
                base = rawBase;
            } else {
                // Non‑standard residue: log warning and normalise to 'N'
                logger.warn("Uncommon residue '{}' (original part '{}') – normalising to 'N'", rawBase, basePart);
                base = "N";
            }

            // Extract position
            if (posStr == null) {
                Matcher posMatcher = POSITION_PATTERN.matcher(basePart);
                if (!posMatcher.find()) {
                    logger.warn("No position number found in '{}'", basePart);
                    return null;
                }
                posStr = posMatcher.group(1);
            }

            return new String[] { base, posStr };
        }
    }
}
