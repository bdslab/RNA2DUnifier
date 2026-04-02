package it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate;

import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarBaseListener;
import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;

public class McAnnotateCustomListener extends McAnnotateGrammarBaseListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;
    private ExtendedRNASecondaryStructure structure;

    private String sequence;
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    @Override
    public void enterMcAnnotateFile(McAnnotateGrammarParser.McAnnotateFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitMcAnnotateFile(McAnnotateGrammarParser.McAnnotateFileContext ctx) {
        this.structure = structureBuilder.build();
    }

    @Override
    public void enterResidueSection(McAnnotateGrammarParser.ResidueSectionContext ctx) {
        this.sequence = "";
    }

    @Override
    public void exitResidueSection(McAnnotateGrammarParser.ResidueSectionContext ctx) {
        this.structureBuilder.setSequence(sequence);
    }

    @Override
    public void enterResidueLine(McAnnotateGrammarParser.ResidueLineContext ctx) {
        String nucleotide = ctx.IDENTIFIER(1).getText();
        this.sequence += nucleotide.length() > 1 ? nucleotide.substring(0,1) : nucleotide;

        int position = Integer.parseInt(ctx.IDENTIFIER(0).getText().substring(1));
        positionMap.put(position, positionMap.size());
    }

    @Override
    public void enterNonAdjacentLine(McAnnotateGrammarParser.NonAdjacentLineContext ctx) {
        this.structureBuilder.addPair(buildPair(ctx.PAIR_ID().getText(), BondType.fromString("stacking")));
    }

    @Override
    public void enterBasePairLine(McAnnotateGrammarParser.BasePairLineContext ctx) {
        this.structureBuilder.addPair(
                buildPair(
                        ctx.PAIR_ID().getText(),
                        getBondType(ctx.ORIENTATION(), ctx.BOND().getFirst().getText()))
        );
    }

    private BondType getBondType(TerminalNode orientation, String bond) {
        if(orientation == null || orientation.getText().isEmpty()) return BondType.UNKNOWN;

        String o = orientation.getText().equals("cis") ? "c" : "t";

        String[] edges = bond.split("/");
        String edge1 = edges[0].substring(0,1);
        String edge2 = edges[1].substring(0,1);

        return BondType.fromString(o+edge1+edge2);
    }

    private Pair buildPair(String pos, BondType bondType) {
        String[] positions = pos.split("-");

        int pos1 = positionMap.get(Integer.parseInt(positions[0].substring(1)));
        int pos2 = positionMap.get(Integer.parseInt(positions[1].substring(1)));
        String nt1 = String.valueOf(sequence.charAt(pos1));
        String nt2 = String.valueOf(sequence.charAt(pos2));

        return new Pair(pos1, pos2, nt1, nt2, bondType);
    }
}
