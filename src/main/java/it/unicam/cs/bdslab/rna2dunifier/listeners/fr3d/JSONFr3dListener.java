package it.unicam.cs.bdslab.rna2dunifier.listeners.fr3d;

import it.unicam.cs.bdslab.JSON.JSONBaseListener;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom ANTLR listener for parsing FR3D JSON output files.
 *
 * <p>This listener processes JSON grammar parse events (from FR3D output)
 * to build an {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Extracting PDB ID and chain ID as header information</li>
 *   <li>Collecting residue sequence IDs from the "modified" and "annotations" arrays</li>
 *   <li>Mapping original sequence IDs to zero‑based indices</li>
 *   <li>Building base‑pair objects from the "annotations" array</li>
 * </ul>
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class JSONFr3dListener extends JSONBaseListener {

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** Builder for the current base pair being processed. */
    private Pair.Builder pairBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** Stack tracking JSON object member names (keys) to maintain context. */
    private final Stack<String> positionStack = new Stack<>();

    /** Flag indicating whether we are inside the "annotations" array. */
    private boolean inAnnotations = false;

    /** Set of original sequence IDs extracted from the JSON. */
    private final Set<Integer> positions = new HashSet<>();

    /** Maps original sequence IDs to zero‑based indices. */
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
        this.structure = structureBuilder.build();
    }

    /**
     * Called when entering an {@code object} rule.
     * If inside the "annotations" array, creates a new {@link Pair.Builder}.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterObject(JSONParser.ObjectContext ctx) {
        if (inAnnotations) {
            pairBuilder = new Pair.Builder();
        }
    }

    /**
     * Called when exiting an {@code object} rule.
     * If inside the "annotations" array, adds the completed pair to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitObject(JSONParser.ObjectContext ctx) {
        if (inAnnotations) {
            structureBuilder.addPair(pairBuilder.build());
        }
    }

    /**
     * Called when entering a {@code member} rule (a key‑value pair in a JSON object).
     * Processes various member names:
     * <ul>
     *   <li>"pdb_id" – stores the PDB ID as header info</li>
     *   <li>"chain_id" – stores the chain ID as header info</li>
     *   <li>"annotations" – enters the annotations section and builds position mapping</li>
     *   <li>"modified" – collects sequence IDs from modified residues</li>
     *   <li>Inside annotations – calls {@link #buildPair(String, JSONParser.MemberContext)}</li>
     * </ul>
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterMember(JSONParser.MemberContext ctx) {
        String memberName = ctx.STRING().getText().replaceAll("\"", "");
        positionStack.push(memberName);
        if (inAnnotations) {
            buildPair(ctx.STRING().getText().replaceAll("\"", ""), ctx);
        } else {
            switch (memberName) {
                case "pdb_id":
                    structureBuilder.addHeaderInfo("PDB ID", ctx.value().STRING().getText());
                    break;
                case "chain_id":
                    structureBuilder.addHeaderInfo("Chain ID", ctx.value().STRING().getText());
                    break;
                case "annotations":
                    enterAnnotations(ctx);
                    break;
                case "modified":
                    addToPositions(ctx.value().array().value().stream()
                            .map(JSONParser.ValueContext::object)
                            .collect(Collectors.toList())
                    );
                    break;
            }
        }
    }

    /**
     * Handles the "annotations" member: enables the annotation flag,
     * collects all sequence IDs from the annotations array, and builds
     * a mapping from original IDs to zero‑based indices.
     *
     * @param ctx the member context containing the annotations array
     */
    private void enterAnnotations(JSONParser.MemberContext ctx) {
        inAnnotations = true;
        addToPositions(ctx.value().array().value().stream()
                .map(JSONParser.ValueContext::object)
                .collect(Collectors.toList()));

        List<Integer> sortedPositions = new ArrayList<>(positions);
        Collections.sort(sortedPositions);

        int i = 0;
        for (Integer position : sortedPositions) {
            positionMap.put(position, i++);
        }
    }

    /**
     * Extracts sequence IDs (seq_id, seq_id1, seq_id2) from a list of JSON objects
     * and adds them to the {@code positions} set.
     *
     * @param objects a list of object contexts (each representing a residue or pair)
     */
    private void addToPositions(List<JSONParser.ObjectContext> objects) {
        objects.forEach(obj ->
                obj.member().stream()
                        .filter(s -> s.STRING().getText().contains("seq_id1") ||
                                s.STRING().getText().contains("seq_id2") ||
                                s.STRING().getText().contains("seq_id"))
                        .forEach(s -> positions.add(Integer.parseInt(s.value().STRING().getText().replaceAll("\"", ""))))
        );
    }

    /**
     * Builds the current {@link Pair} by setting fields based on the member name.
     * Called only when {@code inAnnotations} is true.
     * <p>
     * Recognised member names:
     * <ul>
     *   <li>"seq_id1" – sets the first residue position (mapped)</li>
     *   <li>"seq_id2" – sets the second residue position (mapped)</li>
     *   <li>"nt1" – sets the first nucleotide type</li>
     *   <li>"nt2" – sets the second nucleotide type</li>
     *   <li>"bp" – sets the bond type (from string)</li>
     * </ul>
     *
     * @param item the member name (key)
     * @param ctx  the member context containing the value
     */
    private void buildPair(String item, JSONParser.MemberContext ctx) {
        String val = ctx.value().STRING().getText().replaceAll("\"", "");

        switch (item) {
            case "seq_id1":
                pairBuilder.setPos1(positionMap.get(Integer.parseInt(val)));
                break;
            case "seq_id2":
                pairBuilder.setPos2(positionMap.get(Integer.parseInt(val)));
                break;
            case "nt1":
                pairBuilder.setNucleotide1(val);
                break;
            case "nt2":
                pairBuilder.setNucleotide2(val);
                break;
            case "bp":
                pairBuilder.setType(BondType.fromString(val));
                break;
        }
    }

    /**
     * Called when exiting a {@code member} rule.
     * Pops the member name from the stack and, if the stack becomes empty,
     * exits the annotations mode.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitMember(JSONParser.MemberContext ctx) {
        positionStack.pop();
        if (positionStack.isEmpty()) {
            inAnnotations = false;
        }
    }
}