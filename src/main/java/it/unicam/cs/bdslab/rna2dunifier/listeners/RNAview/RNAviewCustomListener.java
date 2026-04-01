package it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarListener;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;
import java.util.Objects;

public class RNAviewCustomListener implements RNAviewGrammarListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;
    private ExtendedRNASecondaryStructure structure;
    private Pair.Builder pairBuilder;

    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    @Override
    public void enterRnaviewFile(RNAviewGrammarParser.RnaviewFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitRnaviewFile(RNAviewGrammarParser.RnaviewFileContext ctx) {
        this.structure = structureBuilder.build();
    }

    @Override
    public void enterBasePairLine(RNAviewGrammarParser.BasePairLineContext ctx) {
        this.pairBuilder = new Pair.Builder();

        String positionsString = ctx.ASSIGNED_NUMBERS().getText().replaceAll(",", "");
        int[] positions = Arrays.stream(positionsString.split("_"))
                .mapToInt(Integer::parseInt)
                .toArray();

        this.pairBuilder.setPos1(positions[0]-1);
        this.pairBuilder.setPos2(positions[1]-1);
        this.pairBuilder.setNucleotide1(ctx.BASE_PAIR().getText().split("-")[0]);
        this.pairBuilder.setNucleotide2(ctx.BASE_PAIR().getText().split("-")[1]);
    }

    @Override
    public void exitBasePairLine(RNAviewGrammarParser.BasePairLineContext ctx) {
        this.structureBuilder.addPair(pairBuilder.build());
    }

    @Override
    public void enterAnnotation(RNAviewGrammarParser.AnnotationContext ctx) {
        if(ctx.STACKED() == null) {
            this.pairBuilder.setType(getType(ctx.EDGE_PAIR().getText(), ctx.ORIENTATION().getText()));
        } else this.pairBuilder.setType(BondType.fromString("stacking"));
    }

    private BondType getType(String val, String orientation) {
        String o = Objects.equals(orientation, "cis") ? "c" : "t";
        String edge1 = val.substring(0,1);
        String edge2 = val.substring(2,3);

        if(edge1.matches("[.?]") || edge2.matches("[.?]")) {
            return BondType.fromString(null);
        }

        if(edge1.equals(edge2)) {
            if(edge1.matches("[-+]"))
                return BondType.fromString(o+"WW");
        }

        return BondType.fromString(o+edge1+edge2);
    }

    @Override
    public void exitAnnotation(RNAviewGrammarParser.AnnotationContext ctx) {

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
