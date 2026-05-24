package it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarBaseListener;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <p>The actual edge‑pair to bond‑type mapping is delegated to a static inner helper class
 * {@link EdgePairConverter} for better separation of concerns and testability.
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
        if (logger.isDebugEnabled()) logger.debug("Starting to parse RNAview file");
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

        // Parse assigned numbers (e.g., "2_54,")
        String positionsString = ctx.ASSIGNED_NUMBERS().getText().replaceAll(",", "");
        String[] posParts = positionsString.split("_");
        if (posParts.length != 2) {
            logger.warn(
                "Unexpected ASSIGNED_NUMBERS format: '{}' – expected two numbers separated by '_'",
                positionsString
            );
            pairBuilder = null;
            return;
        }

        try {
            int pos1 = Integer.parseInt(posParts[0]) - 1;
            int pos2 = Integer.parseInt(posParts[1]) - 1;
            pairBuilder.setPos1(pos1).setPos2(pos2);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse positions from '{}'", positionsString, e);
            pairBuilder = null;
            return;
        }

        // Parse base pair (e.g., "G-U")
        String basePair = ctx.BASE_PAIR().getText();
        String[] bases = basePair.split("-");
        if (bases.length != 2) {
            logger.warn("Unexpected BASE_PAIR format: '{}' – expected two bases separated by '-'", basePair);
            pairBuilder = null;
            return;
        }
        pairBuilder.setNucleotide1(normalizeResidue(bases[0])).setNucleotide2(normalizeResidue(bases[1]));

        if (logger.isDebugEnabled()) {
            logger.debug("Base pair line: {}–{} ({}‑{})", positionsString, basePair, bases[0], bases[1]);
        }
    }

    private String normalizeResidue(String residue) {
        if (residue == null || residue.isEmpty()) return "N";
        char first = residue.charAt(0);
        return Character.isLowerCase(first) ? "N" : residue;
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
            structureBuilder.addPair(pairBuilder.build());
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
        if (pairBuilder == null) {
            logger.warn("Annotation encountered but no valid pair builder – skipping");
            return;
        }
        if (ctx.STACKED() != null) {
            pairBuilder.setType(BondType.STACKING);
            if (logger.isTraceEnabled()) logger.trace("Annotation: stacking interaction");
        } else {
            String edgePair = ctx.EDGE_PAIR().getText();
            String orientation = ctx.ORIENTATION().getText();
            BondType type = EdgePairConverter.toBondType(edgePair, orientation);
            pairBuilder.setType(type);
            if (logger.isTraceEnabled()) {
                logger.trace("Annotation: edgePair={}, orientation={} → BondType={}", edgePair, orientation, type);
            }
        }
    }

    /**
     * Helper class that converts an RNAview edge pair (e.g., "W/W", "S/H", "-/-")
     * and orientation ("cis"/"trans") into the corresponding {@link BondType}.
     *
     * <p>This class is stateless and thread‑safe; its static mapping tables are built once.
     * The conversion rules are:
     * <ul>
     *   <li>If either edge is '.' or '?' → {@code BondType.UNKNOWN}.</li>
     *   <li>If both edges are identical and are '-' or '+' → canonical pair (cWW or tWW).</li>
     *   <li>Otherwise, the result is constructed as {@code prefix + edge1 + edge2},
     *       where prefix is 'c' for cis and 't' for trans.</li>
     * </ul>
     */
    private static final class EdgePairConverter {

        // No instantiation
        private EdgePairConverter() {
            throw new UnsupportedOperationException("Utility class");
        }

        /**
         * Converts an RNAview edge pair and orientation to a BondType.
         *
         * @param edgePair    the edge pair string (e.g., "W/W", "-/-", "S/H")
         * @param orientation the orientation ("cis" or "trans")
         * @return the corresponding BondType, or BondType.UNKNOWN if not recognised
         */
        static BondType toBondType(String edgePair, String orientation) {
            String prefix = "cis".equals(orientation) ? "c" : "t";
            if (edgePair == null || edgePair.length() < 3 || edgePair.charAt(1) != '/') {
                logger.warn("Malformed EDGE_PAIR string: '{}' – expected format X/Y", edgePair);
                return BondType.UNKNOWN;
            }
            char edge1 = edgePair.charAt(0);
            char edge2 = edgePair.charAt(2);

            // Unknown edge characters
            if (edge1 == '.' || edge2 == '.' || edge1 == '?' || edge2 == '?') {
                logger.warn("Unknown edge character(s) in '{}' – setting bond type to UNKNOWN", edgePair);
                return BondType.UNKNOWN;
            }

            // Special case: '-' or '+' indicate canonical Watson-Crick pairs (WW)
            if (edge1 == edge2 && (edge1 == '-' || edge1 == '+')) {
                return BondType.fromString(prefix + "WW");
            }

            // Default: combine prefix and edges
            return BondType.fromString(prefix + edge1 + edge2);
        }
    }
}
