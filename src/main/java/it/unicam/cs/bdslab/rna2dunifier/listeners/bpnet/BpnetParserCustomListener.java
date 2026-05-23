package it.unicam.cs.bdslab.rna2dunifier.listeners.bpnet;

import it.unicam.cs.bdslab.bpnet.BpnetGrammarBaseListener;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom ANTLR listener for parsing bpnet output files.
 *
 * <p>This listener processes bpnet grammar parse events to build an
 * {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Header lines containing a position and nucleotide (building the sequence)</li>
 *   <li>Pair lines containing two positions and a bond specification</li>
 *   <li>Conversion of bpnet bond notations into internal {@link BondType} format</li>
 * </ul>
 * The listener reconstructs the RNA sequence in order and builds a set of
 * base‑pair interactions.
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class BpnetParserCustomListener extends BpnetGrammarBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(BpnetParserCustomListener.class);

    private static final Map<Character, Character> EDGE_MAP = new HashMap<>();

    static {
        // Direct mapping case‑insensitive for W, H, S
        EDGE_MAP.put('W', 'W');
        EDGE_MAP.put('w', 'W');
        EDGE_MAP.put('H', 'H');
        EDGE_MAP.put('h', 'H');
        EDGE_MAP.put('S', 'S');
        EDGE_MAP.put('s', 'S');
        // Special cases
        EDGE_MAP.put('+', 'W');
        EDGE_MAP.put('z', 'S');
        EDGE_MAP.put('g', 'H');
    }

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** Set of pairs collected during parsing (using a Set to avoid duplicates). */
    private final Set<Pair> pairs = new HashSet<>();

    /** Accumulator for the nucleotide sequence. */
    private final StringBuilder sequence = new StringBuilder();

    /** Current position (1‑based index) from the most recent header line. */
    private int currentPosition;

    /** Current nucleotide character from the most recent header line. */
    private String currentNucleotide;

    /**
     * Returns the parsed RNA secondary structure.
     *
     * @return the built {@link ExtendedRNASecondaryStructure}
     */
    public ExtendedRNASecondaryStructure getStructure() {
        return structureBuilder.build();
    }

    /**
     * Called when entering the root {@code bpnetFile} rule.
     * Initialises the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterBpnetFile(BpnetGrammarParser.BpnetFileContext ctx) {
        structureBuilder = new ExtendedRNASecondaryStructure.Builder();
        logger.debug("Started parsing bpnet file");
    }

    /**
     * Called when exiting the root {@code bpnetFile} rule.
     * Sets the reconstructed sequence and adds all collected pairs to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitBpnetFile(BpnetGrammarParser.BpnetFileContext ctx) {
        structureBuilder.setSequence(sequence.toString());
        pairs.forEach(pair -> structureBuilder.addPair(pair));
        logger.info("Finished bpnet file: sequence length = {}, total pairs = {}", sequence.length(), pairs.size());
    }

    /**
     * Called when entering a {@code pairs} rule (a header line).
     * Extracts the current position and nucleotide, then appends the nucleotide
     * to the sequence builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterPairs(BpnetGrammarParser.PairsContext ctx) {
        currentPosition = Integer.parseInt(ctx.INT().getFirst().getText());
        String currentNucleotideRaw = ctx.TEXT().getFirst().getText();
        currentNucleotide = currentNucleotideRaw.length() > 1 ? "N" : currentNucleotideRaw;

        sequence.append(currentNucleotide);
        logger.trace("Added residue {}: {}", currentPosition, currentNucleotide);
    }

    /**
     * Called when entering a {@code pair} rule (a bond line).
     * Creates a new {@link Pair} from the current header context and the bond line,
     * then adds it to the internal set.
     * <p>
     * Positions are converted from 1‑based (input) to 0‑based (internal model).
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterPair(BpnetGrammarParser.PairContext ctx) {
        BondType type = getType(ctx.BOND().getText());
        if (type == null) {
            logger.warn(
                "Bond type is null for pair {}‑{} (bond={})",
                currentPosition,
                Integer.parseInt(ctx.INT().getFirst().getText()),
                ctx.BOND().getText()
            );
        }
        pairs.add(
            new Pair(
                currentPosition - 1,
                Integer.parseInt(ctx.INT().getFirst().getText()) - 1,
                currentNucleotide,
                ctx.TEXT().getFirst().getText(),
                type
            )
        );
    }

    /**
     * Converts a bpnet bond string into an internal {@link BondType}.
     * <p>
     * The bond format is {@code edge1:edge2C} or {@code edge1:edge2T} (cis/trans).
     * Edges are converted using {@link #convertEdge(String)}.
     *
     * @param bond the raw bond string (e.g., "W:WC" or "H:SC")
     * @return the corresponding {@code BondType}
     */
    private BondType getType(String bond) {
        // bond should have at least 4 characters: e.g. "W:WC"
        char edge1Char = bond.charAt(0);
        char edge2Char = bond.charAt(2);
        char orientChar = bond.charAt(3);

        String edge1 = convertEdge(edge1Char);
        String edge2 = convertEdge(edge2Char);
        String orientation = String.valueOf(orientChar).toLowerCase();

        BondType result = BondType.fromString(orientation + edge1 + edge2);
        if (result == null) {
            logger.warn(
                "BondType.fromString returned null for bond={}, orientation={}, edge1={}, edge2={}",
                bond,
                orientation,
                edge1,
                edge2
            );
        }
        return result;
    }

    /**
     * Converts a single‑character edge code from bpnet format to internal format.
     * <p>
     * Ottimizzazione punto 7: uso di mappa statica precalcolata.
     *
     * @param edge a single character edge code (e.g., 'W', 'z', '+')
     * @return the converted edge letter for internal bond representation, or "?" if unknown
     */
    private static String convertEdge(char edge) {
        Character mapped = EDGE_MAP.get(edge);
        if (mapped != null) return String.valueOf(mapped);

        logger.warn("Unrecognised edge code '{}' – using '?'", edge);
        return "?";
    }
}
