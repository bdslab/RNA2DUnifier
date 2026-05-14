package it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarBaseListener;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Custom ANTLR listener for parsing RNAview output files.
 *
 * <p>This listener processes RNAview grammar parse events to build an
 * {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Base pair lines – extracts positions (from assigned numbers) and nucleotide types</li>
 *   <li>Annotation – determines bond type from edge pair and orientation,
 *       or marks stacking interactions</li>
 *   <li>Conversion of RNAview edge notations into internal {@link BondType} format,
 *       handling special cases like ".", "?", "-", "+"</li>
 * </ul>
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class RNAviewCustomListener extends RNAviewGrammarBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(RNAviewCustomListener.class);

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** Builder for the current base pair being processed. */
    private Pair.Builder pairBuilder;

    /**
     * Returns the parsed RNA secondary structure.
     *
     * @return the built {@link ExtendedRNASecondaryStructure}
     */
    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    /**
     * Called when entering the root {@code rnaviewFile} rule.
     * Initialises the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterRnaviewFile(RNAviewGrammarParser.RnaviewFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
        logger.debug("Starting to parse RNAview file");
    }

    /**
     * Called when exiting the root {@code rnaviewFile} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitRnaviewFile(RNAviewGrammarParser.RnaviewFileContext ctx) {
        this.structure = structureBuilder.build();
        if (structure.getSequence() == null || structure.getSequence().isEmpty()) {
            logger.warn("No sequence was set in the RNAview structure (RNAview files do not contain sequence)");
        }
    }

    /**
     * Called when entering a {@code basePairLine} rule.
     * Creates a new {@link Pair.Builder} and populates it with:
     * <ul>
     *   <li>Positions extracted from {@code ASSIGNED_NUMBERS} (format "1_2," → [1,2])</li>
     *   <li>Nucleotides from {@code BASE_PAIR} (format "A-U")</li>
     * </ul>
     * Positions are converted from 1‑based (input) to 0‑based (internal model).
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterBasePairLine(RNAviewGrammarParser.BasePairLineContext ctx) {
        this.pairBuilder = new Pair.Builder();

        String positionsString = ctx.ASSIGNED_NUMBERS().getText().replaceAll(",", "");
        String[] posParts = positionsString.split("_");
        if (posParts.length != 2) {
            logger.warn("Unexpected ASSIGNED_NUMBERS format: '{}' – expected two numbers separated by '_'", positionsString);
        }

        try {
            int[] positions = Arrays.stream(posParts)
                    .mapToInt(Integer::parseInt)
                    .toArray();
            this.pairBuilder.setPos1(positions[0] - 1);
            this.pairBuilder.setPos2(positions[1] - 1);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse positions from '{}'", positionsString, e);
            return;
        }

        String basePair = ctx.BASE_PAIR().getText();
        String[] bases = basePair.split("-");
        if (bases.length != 2) {
            logger.warn("Unexpected BASE_PAIR format: '{}' – expected two bases separated by '-'", basePair);
        }

        this.pairBuilder.setNucleotide1(getResidue(bases[0]));
        this.pairBuilder.setNucleotide2(getResidue(bases[1]));
        logger.debug("Base pair line: {}–{} ({}‑{})",
                positionsString, basePair, bases[0], bases[1]);
    }

    private String getResidue(String residue) {
        return Character.isLowerCase(residue.charAt(0)) ? "N" : residue;
    }

    /**
     * Called when exiting a {@code basePairLine} rule.
     * Adds the completed pair to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitBasePairLine(RNAviewGrammarParser.BasePairLineContext ctx) {
        if (pairBuilder != null) {
            this.structureBuilder.addPair(pairBuilder.build());
        } else {
            logger.warn("Attempted to exit a base pair line without a valid pairBuilder");
        }
    }

    /**
     * Called when entering an {@code annotation} rule.
     * Determines the bond type:
     * <ul>
     *   <li>If the annotation is "stacked" → uses {@code BondType.fromString("stacking")}</li>
     *   <li>Otherwise → calls {@link #getType(String, String)} to convert edge pair and orientation</li>
     * </ul>
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterAnnotation(RNAviewGrammarParser.AnnotationContext ctx) {
        if (ctx.STACKED() == null) {
            String edgePair = ctx.EDGE_PAIR().getText();
            String orientation = ctx.ORIENTATION().getText();
            BondType type = getType(edgePair, orientation);
            this.pairBuilder.setType(type);
            logger.trace("Annotation: edgePair={}, orientation={} → BondType={}", edgePair, orientation, type);
        } else {
            this.pairBuilder.setType(BondType.fromString("stacking"));
            logger.trace("Annotation: stacking interaction");
        }
    }

    /**
     * Converts an RNAview edge pair and orientation into an internal {@link BondType}.
     * <p>
     * Edge pair format is {@code X/Y} (e.g., "W/W", "S/H"). Orientation is either
     * "cis" (converted to 'c') or "trans" (converted to 't').
     * <p>
     * Special cases:
     * <ul>
     *   <li>If either edge is '.' or '?' → returns {@code BondType.UNKNOWN} (via fromString(null))</li>
     *   <li>If both edges are identical and are '-' or '+' → returns "cWW" (or "tWW")</li>
     *   <li>Otherwise returns the concatenated orientation + edge1 + edge2</li>
     * </ul>
     *
     * @param val         the edge pair string (e.g., "W/W")
     * @param orientation the orientation string ("cis" or "trans")
     * @return the corresponding {@code BondType}
     */
    private BondType getType(String val, String orientation) {
        String o = Objects.equals(orientation, "cis") ? "c" : "t";
        if (val.length() < 3 || val.charAt(1) != '/') {
            logger.warn("Malformed EDGE_PAIR string: '{}' – expected format X/Y", val);
            return BondType.fromString(null);
        }
        String edge1 = val.substring(0, 1);
        String edge2 = val.substring(2, 3);

        if (edge1.matches("[.?]") || edge2.matches("[.?]")) {
            logger.warn("Unknown edge character(s) in '{}' – setting bond type to UNKNOWN", val);
            return BondType.fromString(null);
        }

        if (edge1.equals(edge2) && edge1.matches("[-+]")) {
            // Special case: '-' or '+' edges are treated as cWW/tWW
            return BondType.fromString(o + "WW");
        }

        return BondType.fromString(o + edge1 + edge2);
    }
}