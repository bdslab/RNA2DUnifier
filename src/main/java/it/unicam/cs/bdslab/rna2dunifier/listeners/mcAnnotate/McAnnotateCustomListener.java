package it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate;

import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarBaseListener;
import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom ANTLR listener for parsing mc‑annotate output files.
 *
 * <p>This listener processes mc‑annotate grammar parse events to build an
 * {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Residue section – reconstructs the RNA sequence and builds a position map</li>
 *   <li>Non‑adjacent stacking lines – creates stacking interactions</li>
 *   <li>Base‑pair lines – creates base‑pair interactions with proper bond types</li>
 * </ul>
 * The listener maps original residue numbers to zero‑based indices and
 * reconstructs the sequence from the residue lines.
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class McAnnotateCustomListener extends McAnnotateGrammarBaseListener {

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** Accumulator for the nucleotide sequence. */
    private String sequence;

    /** Maps original residue numbers (from PDB) to zero‑based indices. */
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    /**
     * Returns the parsed RNA secondary structure.
     *
     * @return the built {@link ExtendedRNASecondaryStructure}
     */
    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    /**
     * Called when entering the root {@code mcAnnotateFile} rule.
     * Initialises the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterMcAnnotateFile(McAnnotateGrammarParser.McAnnotateFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    /**
     * Called when exiting the root {@code mcAnnotateFile} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitMcAnnotateFile(McAnnotateGrammarParser.McAnnotateFileContext ctx) {
        this.structure = structureBuilder.build();
    }

    /**
     * Called when entering the {@code residueSection} rule.
     * Initialises the sequence accumulator.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterResidueSection(McAnnotateGrammarParser.ResidueSectionContext ctx) {
        this.sequence = "";
    }

    /**
     * Called when exiting the {@code residueSection} rule.
     * Sets the reconstructed sequence in the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitResidueSection(McAnnotateGrammarParser.ResidueSectionContext ctx) {
        this.structureBuilder.setSequence(sequence);
    }

    /**
     * Called when entering a {@code residueLine} rule.
     * Extracts the nucleotide and position, appends the nucleotide to the sequence,
     * and updates the position map.
     * <p>
     * The first {@code IDENTIFIER} contains the residue identifier (e.g., "A1"),
     * the second {@code IDENTIFIER} contains the nucleotide type.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterResidueLine(McAnnotateGrammarParser.ResidueLineContext ctx) {
        String nucleotide = ctx.IDENTIFIER(1).getText();
        this.sequence += nucleotide.length() > 1 ? nucleotide.substring(0, 1) : nucleotide;

        int position = Integer.parseInt(ctx.IDENTIFIER(0).getText().substring(1));
        positionMap.put(position, positionMap.size());
    }

    /**
     * Called when entering a {@code nonAdjacentLine} rule.
     * Creates a stacking pair (bond type "stacking") and adds it to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterNonAdjacentLine(McAnnotateGrammarParser.NonAdjacentLineContext ctx) {
        this.structureBuilder.addPair(buildPair(ctx.PAIR_ID().getText(), BondType.fromString("stacking")));
    }

    /**
     * Called when entering a {@code basePairLine} rule.
     * Creates a base‑pair interaction with the appropriate bond type and adds it to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterBasePairLine(McAnnotateGrammarParser.BasePairLineContext ctx) {
        this.structureBuilder.addPair(
                buildPair(
                        ctx.PAIR_ID().getText(),
                        getBondType(ctx.ORIENTATION(), ctx.BOND().getFirst().getText()))
        );
    }

    /**
     * Converts an orientation and bond string into an internal {@link BondType}.
     * <p>
     * The bond format is {@code edge1/edge2} (e.g., "W/W"). Orientation is either
     * "cis" (converted to 'c') or "trans" (converted to 't').
     *
     * @param orientation the orientation token (may be null)
     * @param bond        the bond string (e.g., "W/W")
     * @return the corresponding {@code BondType}, or {@link BondType#UNKNOWN} if orientation is missing
     */
    private BondType getBondType(TerminalNode orientation, String bond) {
        if (orientation == null || orientation.getText().isEmpty()) return BondType.UNKNOWN;

        String o = orientation.getText().equals("cis") ? "c" : "t";

        String[] edges = bond.split("/");
        String edge1 = edges[0].substring(0, 1);
        String edge2 = edges[1].substring(0, 1);

        return BondType.fromString(o + edge1 + edge2);
    }

    /**
     * Builds a {@link Pair} object from a pair identifier string and a bond type.
     * <p>
     * The pair identifier format is {@code A1-U2}, where the first part is the
     * first residue and the second part is the second residue. Residue numbers
     * are extracted from the identifiers (e.g., "A1" → position 1) and then
     * mapped through {@code positionMap} to zero‑based indices.
     * Nucleotides are retrieved from the reconstructed sequence.
     *
     * @param pos      the pair identifier (e.g., "A1-U2")
     * @param bondType the type of interaction
     * @return a new {@code Pair} with resolved positions and nucleotides
     */
    private Pair buildPair(String pos, BondType bondType) {
        String[] positions = pos.split("-");

        int pos1 = positionMap.get(Integer.parseInt(positions[0].substring(1)));
        int pos2 = positionMap.get(Integer.parseInt(positions[1].substring(1)));
        String nt1 = String.valueOf(sequence.charAt(pos1));
        String nt2 = String.valueOf(sequence.charAt(pos2));

        return new Pair(pos1, pos2, nt1, nt2, bondType);
    }
}