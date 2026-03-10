package it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnaview.RNAviewParser;
import it.unicam.cs.bdslab.rnaview.RNAviewParserListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Objects;

public class RNAviewParserCustomListener implements RNAviewParserListener {

    ExtendedRNASecondaryStructure.Builder structureBuilder;
    ExtendedRNASecondaryStructure structure;
    Pair.Builder currentPairBuilder;

    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    @Override
    public void enterRnaviewFile(RNAviewParser.RnaviewFileContext ctx) {
        structureBuilder = new ExtendedRNASecondaryStructure.Builder()
                .addHeaderInfo("File name", ctx.FILE_NAME().getText());
    }

    @Override
    public void exitRnaviewFile(RNAviewParser.RnaviewFileContext ctx) {
        structure = structureBuilder.build();
    }

    @Override
    public void enterPairs(RNAviewParser.PairsContext ctx) {
        currentPairBuilder = new Pair.Builder();


    }

    @Override
    public void exitPairs(RNAviewParser.PairsContext ctx) {
        structureBuilder = structureBuilder.addPair(currentPairBuilder.build());
    }

    @Override
    public void enterBase_numbers(RNAviewParser.Base_numbersContext ctx) {
        currentPairBuilder = currentPairBuilder
                .setPos1(Integer.parseInt(ctx.NUMBER().getFirst().getText())-1)
                .setPos2(Integer.parseInt(ctx.NUMBER().getLast().getText())-1); // 0-index
    }

    @Override
    public void exitBase_numbers(RNAviewParser.Base_numbersContext ctx) {

    }

    @Override
    public void enterChain_id(RNAviewParser.Chain_idContext ctx) {

    }

    @Override
    public void exitChain_id(RNAviewParser.Chain_idContext ctx) {

    }

    @Override
    public void enterResidue(RNAviewParser.ResidueContext ctx) {

    }

    @Override
    public void exitResidue(RNAviewParser.ResidueContext ctx) {

    }

    @Override
    public void enterBase_pair(RNAviewParser.Base_pairContext ctx) {
        String nucleotide1 = ctx.BASE_PAIR().getText().substring(0,1);
        String nucleotide2 = ctx.BASE_PAIR().getText().substring(2,3);
        currentPairBuilder = currentPairBuilder
                .setNucleotide1(nucleotide1)
                .setNucleotide2(nucleotide2);
    }

    @Override
    public void exitBase_pair(RNAviewParser.Base_pairContext ctx) {

    }

    @Override
    public void enterPair(RNAviewParser.PairContext ctx) {
        currentPairBuilder = currentPairBuilder.setType(
                getType(ctx.BASE_PAIR_ANNOTATION().getText().strip(),
                        ctx.ORIENTATION().getText())
        );
    }

    @Override
    public void exitPair(RNAviewParser.PairContext ctx) {

    }

    @Override
    public void enterStacked(RNAviewParser.StackedContext ctx) {
        currentPairBuilder = currentPairBuilder.setType(BondType.fromString("stacking"));
    }

    @Override
    public void exitStacked(RNAviewParser.StackedContext ctx) {

    }

    @Override
    public void enterSaenger(RNAviewParser.SaengerContext ctx) {

    }

    @Override
    public void exitSaenger(RNAviewParser.SaengerContext ctx) {

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

        return BondType.fromString(o+edge1.toUpperCase()+edge2.toUpperCase());
    }
}
