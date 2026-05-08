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

    /** Builder for the current base pair being processed. */
    private Pair.Builder pairBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** The sequence string builder */
    private final StringBuilder sequence = new StringBuilder();

    /** Stack tracking JSON object member names (keys) to maintain context. */
    private final Stack<String> positionStack = new Stack<>();

    /** position map to normalize nucleotide positions */
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    /** Flag indicating whether we are inside the "pairs" array. */
    private boolean inPairs = false;

    /** Flag indicating whether we are inside the "nts" array */
    private boolean inNts = false;

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
    }

    /**
     * Called when exiting the root {@code json} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitJson(JSONParser.JsonContext ctx) {
        structureBuilder.setSequence(sequence.toString());
        this.structure = structureBuilder.build();
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
            pairBuilder = new Pair.Builder();
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
        if (inPairs) {
            structureBuilder.addPair(pairBuilder.build());
        }
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
        String val = ctx.STRING().getText().replaceAll("\"", "");
        buildPair(val, ctx);
        positionStack.push(val);
        if (positionStack.size() == 1) {
            switch (positionStack.peek()) {
                case "pairs":
                    inPairs = true;
                    buildPositionMap(ctx);
                    break;
                case "nts":
                    inNts = true;
                    logger.warn("Sequence information ('nts' array) is present in JSON but ignored by this parser.");
                    break;
            }
        }
    }

    /**
     * Builds the current {@link Pair} by setting fields based on the member name.
     * Called only when {@code inPairs} is true.
     * <p>
     * Recognised member names:
     * <ul>
     *   <li>"nt1" – sets first residue: extracts position and nucleotide from string like "A1"</li>
     *   <li>"nt2" – sets second residue: extracts position and nucleotide from string like "C23"</li>
     *   <li>"LW" – sets the bond type (e.g., "cWW", "tSH")</li>
     * </ul>
     * The format for nt1/nt2 values is a single nucleotide letter followed by the position number
     * (e.g., "A1", "G42").
     *
     * @param val the member name (key)
     * @param ctx the member context containing the value
     */
    private void buildPair(String val, JSONParser.MemberContext ctx) {
        if (inPairs) {
            String item;
            switch (val) {
                case "nt1":
                    item = getItem(ctx);
                    String residue1 = extractResidueIdentifier(item);
                    if (residue1 != null) {
                        buildPair(residue1, true);
                    }
                    break;
                case "nt2":
                    item = getItem(ctx);
                    String residue2 = extractResidueIdentifier(item);
                    if (residue2 != null) {
                        buildPair(residue2, false);
                    }
                    break;
                case "LW":
                    item = getItem(ctx);
                    pairBuilder.setType(BondType.fromString(item));
                    break;
            }
        }
    }

    /**
     * Extracts the residue identifier part from a full identifier like "A.A1".
     * Expected format: chain.residue (e.g., "A.A1").
     * If a dot is present, returns the part after the dot.
     *
     * @param fullIdentifier the raw string from the JSON
     * @return the residue identifier (e.g., "A1") or {@code null} if format is invalid
     */
    private String extractResidueIdentifier(String fullIdentifier) {
        if (fullIdentifier == null || fullIdentifier.isEmpty()) {
            logger.warn("Empty or null residue identifier encountered.");
            return null;
        }
        String[] parts = fullIdentifier.split("\\.");
        if (parts.length < 2) {
            logger.warn(
                "Residue identifier '{}' does not contain a dot ('.') – assuming the whole string is the residue part.",
                fullIdentifier
            );
            return fullIdentifier;
        }
        if (parts.length > 2) {
            logger.warn(
                "Residue identifier '{}' contains multiple dots – using part after first dot: '{}'",
                fullIdentifier,
                parts[1]
            );
        }
        return parts[1];
    }

    private void buildPair(String val, boolean nt1) {
        String[] n = extractNucleotideValue(val);

        if (nt1) {
            pairBuilder.setPos1(positionMap.get(Integer.parseInt(n[1])));
            pairBuilder.setNucleotide1(n[0]);
        } else {
            pairBuilder.setPos2(positionMap.get(Integer.parseInt(n[1])));
            pairBuilder.setNucleotide2(n[0]);
        }
    }

    private String[] extractNucleotideValue(String val) {
        String regex = "^(?:([A-Z]+[0-9]+)/([0-9]+)|([A-Z]+)([0-9]+))$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(val);

        String nucleotide = null;
        int index = -1;
        if (matcher.matches()) {
            if (matcher.group(1) != null) {
                // Slash case
                nucleotide = matcher.group(1);
                index += Integer.parseInt(matcher.group(2));
            } else {
                // No-slash case
                nucleotide = matcher.group(3);
                index += Integer.parseInt(matcher.group(4));
            }
        } else {
            logger.warn(
                "Unrecognised residue format: '{}'. Expected patterns: 'LETTERS+DIGITS/DIGITS' or 'LETTERS+DIGITS'",
                val
            );
        }

        if (nucleotide != null && nucleotide.length() > 1) {
            logger.warn(
                "Nucleotide string '{}' has length >1 – truncating to first character (uncommon residue) '{}'.",
                nucleotide,
                nucleotide.substring(0, 1)
            );
            nucleotide = nucleotide.substring(0, 1);
        }

        return new String[] { nucleotide, String.valueOf(index) };
    }

    /**
     * Extracts the string value from a member context (removing surrounding quotes).
     *
     * @param ctx the member context
     * @return the unquoted string value, or an empty string if the value is not a STRING
     */
    private String getItem(JSONParser.MemberContext ctx) {
        if (ctx.value().STRING() != null) {
            return ctx.value().STRING().getText().replaceAll("\"", "");
        } else {
            logger.warn("Expected STRING value but found different type – returning empty string.");
            return "";
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
        positionStack.pop();
        if (positionStack.isEmpty()) {
            inPairs = false;
            inNts = false;
        }
    }

    private void buildPositionMap(JSONParser.MemberContext ctx) {
        Set<Integer> positions = new HashSet<>();
        ctx
            .value()
            .array()
            .value()
            .forEach(value ->
                value
                    .object()
                    .member()
                    .forEach(member -> {
                        String info = member.STRING().getText().replaceAll("\"", "");
                        if (Objects.equals(info, "nt1") || Objects.equals(info, "nt2")) {
                            String val = getItem(member);
                            String[] n = extractNucleotideValue(extractResidueIdentifier(val));
                            positions.add(Integer.parseInt(n[1]));
                        }
                    })
            );

        logger.warn("Normalizing residue positions");

        positions
            .stream()
            .sorted()
            .forEach(position -> positionMap.put(position, positionMap.size()));
    }
}
