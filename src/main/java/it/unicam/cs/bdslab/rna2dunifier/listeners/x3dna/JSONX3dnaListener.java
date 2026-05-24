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
 *   <li>Extracting base‑pair information only from the top‑level {@code "pairs"} array,
 *       ignoring nested arrays with the same name (e.g., inside {@code "helices"} or {@code "stems"}).</li>
 *   <li>Parsing nucleotide identifiers in formats like {@code "A.G2"}, {@code "A.GTP1"}, or {@code "A.A23/76"}
 *       into a nucleotide letter (or {@code "N"} for non‑standard residues) and a PDB position number.</li>
 *   <li>Converting x3dna bond type annotations (e.g., {@code "cWW"}, {@code "tSH"}) into internal {@link BondType}.</li>
 *   <li>Building a zero‑based position map from the PDB numbers to ensure consistent indexing.</li>
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

    /** Maps original PDB residue numbers to zero‑based indices. */
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    /** Flag indicating whether we are inside the top‑level {@code "pairs"} array. */
    private boolean inPairs = false;

    /** Helper for parsing residue identifiers. */
    private ResidueParser residueParser;

    /** Current nesting depth of JSON objects (used to detect top‑level members). */
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
     * Initialises the structure builder and the residue parser.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterJson(JSONParser.JsonContext ctx) {
        structureBuilder = new ExtendedRNASecondaryStructure.Builder();
        residueParser = new ResidueParser(positionMap);
        if (logger.isDebugEnabled()) logger.debug("Started parsing x3dna JSON file");
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
     * If inside the top‑level {@code "pairs"} array, creates a new {@link Pair.Builder}
     * for the upcoming pair object.
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
     * If inside the top‑level {@code "pairs"} array, builds the pair and adds it
     * to the structure builder (if valid).
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
     * Processes the member based on its name and current nesting depth.
     * <ul>
     *   <li>If the member is {@code "pairs"} at depth 1 (top‑level object), activates
     *       the {@code inPairs} flag and builds the position map.</li>
     *   <li>If inside {@code inPairs} and a pair builder exists, processes fields
     *       {@code nt1}, {@code nt2}, and {@code LW} by delegating to the residue parser
     *       or setting the bond type.</li>
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
     * Decrements the depth counter and, if the member is the top‑level {@code "pairs"},
     * disables the {@code inPairs} flag.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitMember(JSONParser.MemberContext ctx) {
        depth--;
        String key = stripQuotes(ctx.STRING().getText());
        if (logger.isDebugEnabled()) logger.debug("Exited pairs");
        if (key.equals("pairs")) {
            inPairs = false;
        }
    }

    /**
     * Builds the position map by scanning the top‑level {@code "pairs"} array.
     * Collects all unique PDB residue numbers from {@code nt1} and {@code nt2} fields,
     * sorts them, and assigns a zero‑based index to each.
     *
     * @param ctx the member context of the {@code "pairs"} key
     */
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
        if (logger.isDebugEnabled()) logger.debug("Position map built with {} entries", positionMap.size());
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Removes leading and trailing double quotes from a string.
     *
     * @param s the input string
     * @return the unquoted string
     */
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

    // ----------------------------------------------------------------------
    // Helper class for residue parsing
    // ----------------------------------------------------------------------

    /**
     * Helper class for parsing x3dna residue identifiers.
     * <p>
     * Recognised formats:
     * <ul>
     *   <li>Standard: {@code "G2"} → base = {@code "G"}, position = {@code 2}</li>
     *   <li>Uncommon residue (long name): {@code "GTP1"} → base = {@code "N"} (normalised), position = {@code 1}</li>
     *   <li>Slash format: {@code "A23/76"} → base = {@code "N"} (normalised), position = {@code 76}</li>
     * </ul>
     * All identifiers are expected to be prefixed by a chain identifier and a dot,
     * e.g., {@code "A.G2"}. The dot and chain part are stripped before parsing.
     */
    private static final class ResidueParser {

        private static final Pattern BASE_PATTERN = Pattern.compile("^([A-Z]+)");
        private static final Pattern POSITION_PATTERN = Pattern.compile("^[A-Z]+([0-9]+)");

        private final Map<Integer, Integer> positionMap;

        /**
         * Creates a new residue parser with the given position mapping.
         *
         * @param positionMap mapping from original PDB numbers to zero‑based indices
         */
        ResidueParser(Map<Integer, Integer> positionMap) {
            this.positionMap = positionMap;
        }

        /**
         * Extracts and sets residue information (position and nucleotide) into a {@link Pair.Builder}.
         *
         * @param builder        the {@code Pair.Builder} to update
         * @param fullIdentifier the full string (e.g., {@code "A.G2"})
         * @param isFirst        {@code true} for {@code nt1}, {@code false} for {@code nt2}
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
         * Extracts just the PDB position number from a full identifier.
         *
         * @param fullIdentifier the full string (e.g., {@code "A.G2"})
         * @return the original PDB position number, or {@code null} if extraction fails
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
         * Extracts the residue part (everything after the first dot) from a full identifier.
         *
         * @param full the full identifier (e.g., {@code "A.G2"})
         * @return the residue part (e.g., {@code "G2"}) or {@code null} if input is invalid
         */
        private String extractIdentifier(String full) {
            if (full == null || full.isEmpty()) return null;
            String[] parts = full.split("\\.");
            if (parts.length < 2) {
                logger.warn("No dot in '{}', using as is (may cause issues)", full);
                return full;
            }
            // In case of multiple dots, take everything after the first dot
            return full.substring(full.indexOf('.') + 1);
        }

        /**
         * Parses a residue string into an array of [base, position].
         * Handles formats: {@code "G2"}, {@code "GTP1"}, {@code "A23/76"}.
         *
         * @param residue the residue string (without chain prefix)
         * @return a two‑element array: [base (or "N" for non‑standard), position as string],
         *         or {@code null} if parsing fails
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
                logger.warn("Uncommon residue '{}' (original part '{}') – normalising to 'N'", rawBase, basePart);
                base = "N";
            }

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
