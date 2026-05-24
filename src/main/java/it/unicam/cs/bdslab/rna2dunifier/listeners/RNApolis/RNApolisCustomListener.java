package it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarBaseListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarParser;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom ANTLR listener for parsing RNApolis output files.
 *
 * <p>This listener processes RNApolis grammar parse events to build a list of
 * {@link ExtendedRNASecondaryStructure} objects (one per strand section). It handles:
 * <ul>
 *   <li>Strand sections – each with a header, a nucleotide sequence, and interaction lines</li>
 *   <li>Header – stores the strand name as header information</li>
 *   <li>Sequence – sets the RNA sequence for the current structure</li>
 *   <li>Interaction – parses dot‑bracket‑like notation with extended symbol pairs
 *       (parentheses, brackets, braces, angle brackets, and letter pairs) to build
 *       base‑pair interactions</li>
 * </ul>
 * The listener supports multi‑strand files and returns a list of structures.
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class RNApolisCustomListener extends RNApolisGrammarBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(RNApolisCustomListener.class);

    /** List of all secondary structures parsed from the input (one per strand). */
    private final List<ExtendedRNASecondaryStructure> structures = new ArrayList<>();

    /** Builder for the current strand being processed. */
    private ExtendedRNASecondaryStructure.Builder currentStructureBuilder;

    /** Nucleotide sequence of the current strand (used to retrieve base letters). */
    private String currentSequence;

    private final InteractingPairBuilder pairBuilder = new InteractingPairBuilder();

    /**
     * Returns the list of parsed RNA secondary structures.
     *
     * @return a list of {@link ExtendedRNASecondaryStructure} objects (one per strand)
     */
    public List<ExtendedRNASecondaryStructure> getStructures() {
        return structures;
    }

    /**
     * Called when entering a {@code strandSection} rule.
     * Initialises a new structure builder for the current strand.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterStrandSection(RNApolisGrammarParser.StrandSectionContext ctx) {
        this.currentStructureBuilder = new ExtendedRNASecondaryStructure.Builder();
        if (logger.isDebugEnabled()) logger.debug("Starting new strand section");
    }

    /**
     * Called when exiting a {@code strandSection} rule.
     * Builds the structure for the current strand and adds it to the list.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitStrandSection(RNApolisGrammarParser.StrandSectionContext ctx) {
        ExtendedRNASecondaryStructure structure = this.currentStructureBuilder.build();
        this.structures.add(structure);
        logger.info(
            "Finished strand: {} with {} pairs",
            structure.getHeaderInfo().getOrDefault("strand_name", "unknown"),
            structure.getPairs().size()
        );
    }

    /**
     * Called when entering a {@code header} rule.
     * Stores the strand name (extracted from the header string, removing the leading '>')
     * as header information.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterHeader(RNApolisGrammarParser.HeaderContext ctx) {
        String header = ctx.HEADER_STRING().getText().substring(1);
        this.currentStructureBuilder.addHeaderInfo("strand_name", header);
        if (logger.isDebugEnabled()) logger.debug("Header: {}", header);
    }

    /**
     * Called when entering a {@code sequence} rule.
     * Stores the nucleotide sequence for the current strand.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterSequence(RNApolisGrammarParser.SequenceContext ctx) {
        this.currentSequence = ctx.NUCLEOTIDE_SEQUENCE().getText();
        this.currentStructureBuilder.setSequence(this.currentSequence);
        if (logger.isDebugEnabled()) logger.debug("Sequence length: {}", this.currentSequence.length());
    }

    /**
     * Called when entering an {@code interaction} rule.
     * Sets the current interaction type (e.g., "cWW", "tSH") and then
     * parses the interaction sequence to build all base pairs.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterInteraction(RNApolisGrammarParser.InteractionContext ctx) {
        String typeStr = ctx.INTERACTION_TYPE().getText();
        // Bond type for the current interaction line (applies to all pairs in that line).
        BondType currentInteractionType = BondType.fromString(typeStr);
        if (currentInteractionType == BondType.UNKNOWN) {
            logger.warn("Unknown interaction type '{}' – pairs will have unknown BondType", typeStr);
        }
        String interactionSeq = ctx.INTERACTION_SEQUENCE().getText();
        if (currentSequence != null && interactionSeq.length() != currentSequence.length()) {
            logger.warn(
                "Interaction sequence length ({}) does not match nucleotide sequence length ({}). " +
                    "Pairs may be out of bounds.",
                interactionSeq.length(),
                currentSequence.length()
            );
        }

        List<Pair> pairs = pairBuilder.buildPairs(interactionSeq, currentSequence, currentInteractionType);

        pairs.forEach(pair -> currentStructureBuilder.addPair(pair));
    }

    private static final class InteractingPairBuilder {

        private static final Map<Character, Character> OPEN_TO_CLOSE = new HashMap<>();
        private static final Map<Character, Character> CLOSE_TO_OPEN = new HashMap<>();

        static {
            char[][] pairs = { { '(', ')' }, { '[', ']' }, { '{', '}' }, { '<', '>' } };
            for (char[] p : pairs) {
                OPEN_TO_CLOSE.put(p[0], p[1]);
                CLOSE_TO_OPEN.put(p[1], p[0]);
            }
            for (char c = 'A'; c <= 'Z'; c++) {
                OPEN_TO_CLOSE.put(c, Character.toLowerCase(c));
                CLOSE_TO_OPEN.put(Character.toLowerCase(c), c);
            }
        }

        /**
         * Parses an interaction sequence and returns a list of base pairs.
         *
         * @param interactionSeq the dot-bracket-like string (e.g., "((..))", "(A..a)")
         * @param nucleotideSeq  the nucleotide sequence (for base symbols)
         * @param bondType       the bond type for all generated pairs
         * @return list of Pair objects (indices are 0‑based)
         */
        public List<Pair> buildPairs(String interactionSeq, String nucleotideSeq, BondType bondType) {
            if (nucleotideSeq == null) {
                logger.warn("No nucleotide sequence provided; cannot build pairs.");
                return Collections.emptyList();
            }

            List<Pair> pairs = new ArrayList<>();
            final int seqLen = nucleotideSeq.length();
            final char[] chars = interactionSeq.toCharArray();
            final Map<Character, Deque<Integer>> stacks = new HashMap<>();

            for (int i = 0; i < chars.length; i++) {
                char symbol = chars[i];
                if (symbol == '.') continue;

                if (OPEN_TO_CLOSE.containsKey(symbol)) {
                    stacks.computeIfAbsent(symbol, k -> new ArrayDeque<>()).push(i);
                } else if (CLOSE_TO_OPEN.containsKey(symbol)) {
                    Pair p = tryCreatePair(stacks, symbol, i, nucleotideSeq, seqLen, bondType);
                    if (p != null) {
                        pairs.add(p);
                    }
                } else {
                    logger.warn("Unrecognised character '{}' at position {} – ignoring", symbol, i);
                }
            }

            logUnmatchedOpenings(stacks);
            return pairs;
        }

        private Pair tryCreatePair(
            Map<Character, Deque<Integer>> stacks,
            char closingSymbol,
            int closePos,
            String nucleotideSeq,
            int seqLen,
            BondType bondType
        ) {
            char openChar = CLOSE_TO_OPEN.get(closingSymbol);
            Deque<Integer> stack = stacks.get(openChar);

            if (stack == null || stack.isEmpty()) {
                logger.warn("Closing symbol '{}' at position {} has no matching opening", closingSymbol, closePos);
                return null;
            }

            int openPos = stack.pop();
            if (!(openPos < seqLen && closePos < seqLen)) {
                logger.warn("Skipping pair at ({}, {}) – out of sequence length {}", openPos, closePos, seqLen);
                return null;
            }

            if (logger.isTraceEnabled()) {
                logger.trace(
                    "Added pair: {}-{} ({}-{})",
                    openPos,
                    closePos,
                    nucleotideSeq.charAt(openPos),
                    nucleotideSeq.charAt(closePos)
                );
            }

            return new Pair(
                openPos,
                closePos,
                Character.toString(nucleotideSeq.charAt(openPos)),
                Character.toString(nucleotideSeq.charAt(closePos)),
                bondType
            );
        }

        private void logUnmatchedOpenings(Map<Character, Deque<Integer>> stacks) {
            for (Map.Entry<Character, Deque<Integer>> e : stacks.entrySet()) {
                if (!e.getValue().isEmpty()) {
                    logger.warn(
                        "Symbol '{}' has {} unmatched opening(s): {}",
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue()
                    );
                }
            }
        }
    }
}
