package it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarBaseListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarParser;

import java.util.*;

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

    /** List of all secondary structures parsed from the input (one per strand). */
    private final List<ExtendedRNASecondaryStructure> structures = new ArrayList<>();

    /** Builder for the current strand being processed. */
    private ExtendedRNASecondaryStructure.Builder currentStructureBuilder;

    /** Bond type for the current interaction line (applies to all pairs in that line). */
    private BondType currentInteractionType;

    /** Nucleotide sequence of the current strand (used to retrieve base letters). */
    private String currentSequence;

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
    }

    /**
     * Called when exiting a {@code strandSection} rule.
     * Builds the structure for the current strand and adds it to the list.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitStrandSection(RNApolisGrammarParser.StrandSectionContext ctx) {
        this.structures.add(this.currentStructureBuilder.build());
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
        this.currentStructureBuilder.addHeaderInfo("strand_name", ctx.HEADER_STRING().getText().substring(1));
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
        this.currentInteractionType = BondType.fromString(ctx.INTERACTION_TYPE().getText());
        buildPairs(ctx.INTERACTION_SEQUENCE().getText());
    }

    /**
     * Parses an interaction sequence string and builds base pairs.
     * <p>
     * The interaction sequence uses a dot‑bracket‑like notation where:
     * <ul>
     *   <li>Opening symbols: {@code ( [ { < } and uppercase letters A‑Z</li>
     *   <li>Closing symbols: {@code ) ] } > } and lowercase letters a‑z</li>
     *   <li>Uppercase letter X pairs with lowercase letter x</li>
     * </ul>
     * The method uses stacks to match opening and closing symbols. When a closing
     * symbol is encountered, it creates a {@link Pair} with the matching opening
     * position and assigns the current interaction type.
     *
     * @param interactionSequence the interaction string (e.g., "((..))", "(A..a)")
     */
    private void buildPairs(String interactionSequence) {
        // Map open -> close
        Map<Character, Character> openToClose = new HashMap<>();
        openToClose.put('(', ')');
        openToClose.put('[', ']');
        openToClose.put('{', '}');
        openToClose.put('<', '>');
        for (char c = 'A'; c <= 'Z'; c++) {
            openToClose.put(c, Character.toLowerCase(c));
        }

        // Map close -> open (fast lookup)
        Map<Character, Character> closeToOpen = new HashMap<>();
        for (Map.Entry<Character, Character> e : openToClose.entrySet()) {
            closeToOpen.put(e.getValue(), e.getKey());
        }

        // Open symbols stacks
        Map<Character, Stack<Integer>> stacks = new HashMap<>();

        // Iteration pattern
        for (int i = 0; i < interactionSequence.length(); i++) {
            char symbol = interactionSequence.charAt(i);

            if (openToClose.containsKey(symbol)) {
                // push to corresponding stack
                stacks.computeIfAbsent(symbol, k -> new Stack<>()).push(i);
            }
            else if (closeToOpen.containsKey(symbol)) {
                // find opening symbol
                char openChar = closeToOpen.get(symbol);
                Stack<Integer> stack = stacks.get(openChar);
                if (stack != null && !stack.isEmpty()) {
                    int openPos = stack.pop(); // last opening position
                    // create pair
                    this.currentStructureBuilder.addPair(
                            new Pair(openPos, i, // 0-index
                                    String.valueOf(currentSequence.charAt(openPos)),
                                    String.valueOf(currentSequence.charAt(i)),
                                    currentInteractionType)
                    );
                } else {
                    // No opening found ERROR!
                    System.err.println("Closing without opening: " + symbol + " at position " + i);
                }
            }
        }

        // Check opening not closed
        for (Map.Entry<Character, Stack<Integer>> e : stacks.entrySet()) {
            if (!e.getValue().isEmpty()) {
                System.err.println("Symbol " + e.getKey() + " has " + e.getValue().size() + " opening not closed");
            }
        }
    }
}