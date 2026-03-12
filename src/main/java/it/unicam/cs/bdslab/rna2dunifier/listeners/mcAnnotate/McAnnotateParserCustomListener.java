package it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate;

import it.unicam.cs.bdslab.mcannotate.McAnnotateParser;
import it.unicam.cs.bdslab.mcannotate.McAnnotateParserListener;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;

public class McAnnotateParserCustomListener implements McAnnotateParserListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    private String sequence;
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    public ExtendedRNASecondaryStructure getStructure() {
        return structureBuilder.build();
    }

    @Override
    public void enterMcAannotateFile(McAnnotateParser.McAannotateFileContext ctx) {
        structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitMcAannotateFile(McAnnotateParser.McAannotateFileContext ctx) {

    }

    @Override
    public void enterResidueSection(McAnnotateParser.ResidueSectionContext ctx) {
        sequence = "";
    }

    @Override
    public void exitResidueSection(McAnnotateParser.ResidueSectionContext ctx) {
        structureBuilder.setSequence(sequence);
    }

    @Override
    public void enterResidueElement(McAnnotateParser.ResidueElementContext ctx) {
        if(ctx.NUCLEOTIDE() != null) {
            sequence += (ctx.NUCLEOTIDE().getText());
            int position = Integer.parseInt(ctx.STRAND_POSITION().getText().substring(1));
            positionMap.put(position, positionMap.size());
        }
    }

    @Override
    public void exitResidueElement(McAnnotateParser.ResidueElementContext ctx) {
    }

    @Override
    public void enterAdjStackingSection(McAnnotateParser.AdjStackingSectionContext ctx) {
    }

    @Override
    public void exitAdjStackingSection(McAnnotateParser.AdjStackingSectionContext ctx) {
    }

    @Override
    public void enterAdjStackingElement(McAnnotateParser.AdjStackingElementContext ctx) {
    }

    @Override
    public void exitAdjStackingElement(McAnnotateParser.AdjStackingElementContext ctx) {
    }

    @Override
    public void enterNonAdjStackingSection(McAnnotateParser.NonAdjStackingSectionContext ctx) {
    }

    @Override
    public void exitNonAdjStackingSection(McAnnotateParser.NonAdjStackingSectionContext ctx) {
    }

    @Override
    public void enterNonAdjStackingElement(McAnnotateParser.NonAdjStackingElementContext ctx) {
        structureBuilder.addPair(buildPair(ctx.NAS_STACK().getText(), BondType.fromString("stacking")));
    }

    @Override
    public void exitNonAdjStackingElement(McAnnotateParser.NonAdjStackingElementContext ctx) {
    }

    @Override
    public void enterBasePairsSection(McAnnotateParser.BasePairsSectionContext ctx) {
    }

    @Override
    public void exitBasePairsSection(McAnnotateParser.BasePairsSectionContext ctx) {
    }

    @Override
    public void enterBasePairsElement(McAnnotateParser.BasePairsElementContext ctx) {

        structureBuilder
                .addPair(buildPair(ctx.POSITION_PAIR().getText(),
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
        String nucl1 = String.valueOf(sequence.charAt(pos1));
        String nucl2 = String.valueOf(sequence.charAt(pos2));

        return new Pair(pos1, pos2, nucl1, nucl2, bondType);
    }

    @Override
    public void exitBasePairsElement(McAnnotateParser.BasePairsElementContext ctx) {
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
    }
}
