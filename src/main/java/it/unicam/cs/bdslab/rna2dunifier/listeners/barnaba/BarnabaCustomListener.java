package it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba;

import it.unicam.cs.bdslab.barnaba.BarnabaGrammarBaseListener;
import it.unicam.cs.bdslab.barnaba.BarnabaGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;

import java.util.*;

/**
 * Custom ANTLR listener for parsing Barnaba output files.
 *
 * <p>This listener processes Barnaba grammar parse events to build an
 * {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Residue specifications (nucleotide type and position mapping)</li>
 *   <li>Interaction lines (base‑pair and stacking annotations)</li>
 *   <li>Comment lines containing sequence information, PDB file names,
 *       and skipping directives for uncommon residues</li>
 * </ul>
 * The listener reconstructs the complete RNA sequence (inserting 'N' for
 * skipped residues) and builds a list of base‑pair interactions with
 * appropriate bond types.
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class BarnabaCustomListener extends BarnabaGrammarBaseListener {

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** Builder for the current base pair being processed. */
    private Pair.Builder pairBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** Maps original PDB residue numbers to zero‑based indices in the reconstructed sequence. */
    private final Map<Integer, Integer> nucleotidePositionMap = new HashMap<>();

    /** List of nucleotide characters building the sequence. */
    private final List<String> sequence = new ArrayList<>();

    /** Number of uncommon residues skipped so far. */
    private int uncommonResidues = 0;

    /** Last processed PDB residue number. */
    private int lastPosition = 0;

    /** Cumulative offset to align original positions with reconstructed indices. */
    private int differencePosition = 0;

    /** Flag indicating whether the first nucleotide of the current pair has been set. */
    private boolean nt1Viewed = false;

    /**
     * Returns the parsed RNA secondary structure.
     *
     * @return the built {@link ExtendedRNASecondaryStructure}
     */
    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    /**
     * Called when entering the root {@code barnabaFile} rule.
     * Initialises the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterBarnabaFile(BarnabaGrammarParser.BarnabaFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    /**
     * Called when exiting the root {@code barnabaFile} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitBarnabaFile(BarnabaGrammarParser.BarnabaFileContext ctx) {
        this.structure = structureBuilder.build();
    }

    /**
     * Called when entering a {@code residueSpec} rule (a single nucleotide specification).
     * Sets either the first or second nucleotide of the current pair based on the {@code nt1Viewed} flag.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterResidueSpec(BarnabaGrammarParser.ResidueSpecContext ctx) {
        int pos = nucleotidePositionMap.get(Integer.parseInt(ctx.INT().getFirst().getText()));
        if (!nt1Viewed) {
            nt1Viewed = true;
            this.pairBuilder.setNucleotide1(ctx.NUCLEOTIDE().getText());
            this.pairBuilder.setPos1(pos);
        } else {
            nt1Viewed = false;
            this.pairBuilder.setNucleotide2(ctx.NUCLEOTIDE().getText());
            this.pairBuilder.setPos2(pos);
        }
    }

    /**
     * Called when entering an {@code interactionLine} rule.
     * Creates a new {@link Pair.Builder} and sets its bond type from the annotation.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterInteractionLine(BarnabaGrammarParser.InteractionLineContext ctx) {
        this.pairBuilder = new Pair.Builder();
        this.pairBuilder.setType(getBondType(ctx.ANNOTATION().getText()));
    }

    /**
     * Converts a Barnaba annotation string into a {@link BondType}.
     * <p>
     * Stacking annotations (e.g., {@code >>}) are mapped to {@code "stacking"}.
     * Base‑pair annotations like {@code WCc} are transformed to {@code cWW}
     * (the internal bond type format).
     *
     * @param annotation the raw annotation from the grammar (e.g., "WWc", ">>", "GUc")
     * @return the corresponding {@code BondType}
     */
    private BondType getBondType(String annotation) {
        if (annotation.matches("[<>][<>]")) {
            return BondType.fromString("stacking");
        } else {
            String pairs = annotation.substring(0, 2);
            if (pairs.equals("WC") || pairs.equals("GU")) {
                // WWc pairs between complementary bases are called WCc or GUc.
                annotation = annotation.replace(pairs, "WW");
            }
            String lastChar = annotation.substring(annotation.length() - 1);
            String prefix = annotation.substring(0, annotation.length() - 1);
            return BondType.fromString(lastChar + prefix);
        }
    }

    /**
     * Called when exiting an {@code interactionLine} rule.
     * Adds the completed pair to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitInteractionLine(BarnabaGrammarParser.InteractionLineContext ctx) {
        Pair pair = pairBuilder.build();
        if (Math.abs(pair.getPos1() - pair.getPos2()) > 1 || !pair.getType().equals(BondType.STACKING))
            this.structureBuilder.addPair(pair);
    }

    /**
     * Called when entering a {@code commentLine} rule.
     * Processes three types of comments:
     * <ul>
     *   <li>"Skipping" – increments the counter of uncommon residues</li>
     *   <li>"sequence" – reconstructs the full RNA sequence, filling gaps with 'N'</li>
     *   <li>"PDB" – stores the PDB file name as header information</li>
     * </ul>
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterCommentLine(BarnabaGrammarParser.CommentLineContext ctx) {
        String comment = ctx.COMMENT().getText().replace("#", "").trim();
        String[] splittedComment = comment.split(" ");

        switch (splittedComment[0]) {
            case "Skipping":
                uncommonResidues++;
                break;
            case "sequence":
                String sequenceRaw = splittedComment[1];
                Arrays.stream(sequenceRaw.split("-")).forEach(this::enterSequenceElement);
                StringBuilder seq = new StringBuilder();
                sequence.forEach(seq::append);
                while (uncommonResidues-- > 0) {
                    // Fill position of uncommon residue
                    seq.append("N");
                }
                structureBuilder = structureBuilder.setSequence(seq.toString());
                break;
            case "PDB":
                this.structureBuilder.addHeaderInfo("File name", splittedComment[1]);
                break;
        }
    }

    /**
     * Processes a single element of the sequence comment (e.g., "A_1").
     * Maps the original PDB residue number to a zero‑based index,
     * handles gaps caused by skipped residues, and builds the nucleotide list.
     *
     * @param element a string in the format "NUCLEOTIDE_position" (e.g., "A_42")
     */
    private void enterSequenceElement(String element) {
        String[] elements = element.split("_");

        String nucleotide = elements[0];

        int elPosition = Integer.parseInt(elements[1]);
        int i = 0;

        if (nucleotidePositionMap.isEmpty()) {
            // Fill position of uncommon residue before the first element
            int gapStartPosition = elPosition - 1;
            if (elPosition > 1) {
                while (gapStartPosition > 0 && uncommonResidues > 0) {
                    gapStartPosition--;
                    uncommonResidues--;
                    nucleotidePositionMap.put(i, i); // 0-index
                    appendNucleotide("N", i++);
                }
            }
            differencePosition = gapStartPosition + 1;
            lastPosition = elPosition;
        } else if (elPosition - lastPosition > 1) {
            // Fill position of uncommon residue
            int difference = elPosition - lastPosition;
            while (difference > 1 && uncommonResidues > 0) {
                difference--;
            }

            /*
             * Remove the jump in the sequence
             * from: GGGCUGUUUUUCUCGCUGACUUUCAGCCC       CAAACAAAAAAUGUCAGCA
             * to:   GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAUGUCAGCA
             */
            differencePosition += elPosition - lastPosition - 1;
            lastPosition = elPosition;
            i = elPosition - differencePosition;
        } else {
            lastPosition = elPosition;
            i = elPosition - differencePosition;
        }

        nucleotidePositionMap.put(elPosition, i); // 0-index
        appendNucleotide(nucleotide, i);
    }

    /**
     * Appends a nucleotide character at the given index, padding the sequence list
     * with spaces if necessary to ensure the index is within bounds.
     *
     * @param nucleotide the nucleotide character (e.g., "A", "C", "G", "U", "N")
     * @param index      the zero‑based position at which to insert the nucleotide
     */
    private void appendNucleotide(String nucleotide, int index) {
        while (sequence.size() < index) {
            sequence.add(" ");
        }
        sequence.add(nucleotide);
    }
}