package it.unicam.cs.bdslab.rna2dunifier.listeners.x3dna;

import it.unicam.cs.bdslab.JSON.JSONBaseListener;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;

import java.util.Stack;

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
        buildSequence(val, ctx);
        positionStack.push(val);
        if (positionStack.size() == 1) {
            if(positionStack.peek().equals("pairs")) {
                inPairs = true;
            } else if(positionStack.peek().equals("nts")) {
                inNts = true;
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
                    pairBuilder.setPos1(Integer.parseInt(item.substring(3)));
                    pairBuilder.setNucleotide1(item.substring(2, 3));
                    break;
                case "nt2":
                    item = getItem(ctx);
                    pairBuilder.setPos2(Integer.parseInt(item.substring(3)));
                    pairBuilder.setNucleotide2(item.substring(2, 3));
                    break;
                case "LW":
                    item = getItem(ctx);
                    pairBuilder.setType(BondType.fromString(item));
                    break;
            }
        }
    }

    private void buildSequence(String val, JSONParser.MemberContext ctx) {
        if(inNts && val.equals("nt_name")) {
            sequence.append(getItem(ctx));
        }
    }

    /**
     * Extracts the string value from a member context (removing surrounding quotes).
     *
     * @param ctx the member context
     * @return the unquoted string value
     */
    private String getItem(JSONParser.MemberContext ctx) {
        return ctx.value().STRING().getText().replaceAll("\"", "");
    }

    /**
     * Called when exiting a {@code member} rule.
     * Pops the member name from the stack and, if the stack becomes empty,
     * exits the "pairs" mode.
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
}