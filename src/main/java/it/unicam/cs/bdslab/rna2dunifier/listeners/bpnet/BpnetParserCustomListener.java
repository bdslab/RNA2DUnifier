package it.unicam.cs.bdslab.rna2dunifier.listeners.bpnet;

import it.unicam.cs.bdslab.bpnet.BpnetGrammarBaseListener;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;

import java.util.HashSet;
import java.util.Set;

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
        currentNucleotide = String.valueOf(ctx.TEXT().getFirst().getText().charAt(0));

        sequence.append(currentNucleotide);
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
        pairs.add(new Pair(
                currentPosition - 1,
                Integer.parseInt(ctx.INT().getFirst().getText()) - 1,
                currentNucleotide,
                ctx.TEXT().getFirst().getText(),
                getType(ctx.BOND().getText()))
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
        String edge1 = convertEdge(bond.substring(0, 1));
        String edge2 = convertEdge(bond.substring(2, 3));
        String orientation = bond.substring(3).toLowerCase();

        return BondType.fromString(orientation + edge1 + edge2);
    }

    /**
     * Converts a single‑character edge code from bpnet format to internal format.
     * <p>
     * Mapping rules:
     * <ul>
     *   <li>{@code W}, {@code H}, {@code S} (case‑insensitive) → uppercase same letter</li>
     *   <li>{@code +} → {@code W}</li>
     *   <li>{@code z} → {@code S}</li>
     *   <li>{@code g} → {@code H}</li>
     *   <li>any other → {@code ?}</li>
     * </ul>
     *
     * @param edge a single character edge code (e.g., "W", "z", "+")
     * @return the converted edge letter for internal bond representation
     */
    private String convertEdge(String edge) {
        if (edge.toLowerCase().matches("[whs]")) return edge.toUpperCase();
        return switch (edge) {
            case "+" -> "W";
            case "z" -> "S";
            case "g" -> "H";
            default -> "?";
        };
    }
}