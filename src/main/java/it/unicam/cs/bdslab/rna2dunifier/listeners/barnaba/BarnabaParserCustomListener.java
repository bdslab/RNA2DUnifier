package it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba;

import it.unicam.cs.bdslab.barnaba.BarnabaParser;
import it.unicam.cs.bdslab.barnaba.BarnabaParserListener;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarnabaParserCustomListener implements BarnabaParserListener {
    private ExtendedRNASecondaryStructure structure;
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    Map<Integer, Integer> nucleotidePositionMap = new HashMap<>();
    List<String> sequence = new ArrayList<>();
    int lastPosition = 0;
    int differencePosition = 0;

    private BarnabaParser.ResidueContext currentResidue1;
    private BarnabaParser.ResidueContext currentResidue2;
    private String currentBondType;


    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    @Override
    public void enterBarnabaFile(BarnabaParser.BarnabaFileContext ctx) {
        structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitBarnabaFile(BarnabaParser.BarnabaFileContext ctx) {
        structure =  structureBuilder.build();
    }

    @Override
    public void enterFileName(BarnabaParser.FileNameContext ctx) {
        structureBuilder = structureBuilder.addHeaderInfo("File", ctx.FILE_NAME().getText());
    }

    @Override
    public void exitFileName(BarnabaParser.FileNameContext ctx) {

    }

    @Override
    public void enterSequenceList(BarnabaParser.SequenceListContext ctx) {

    }

    @Override
    public void exitSequenceList(BarnabaParser.SequenceListContext ctx) {

    }

    @Override
    public void enterSequence(BarnabaParser.SequenceContext ctx) {
        // TODO: build sequence

    }

    @Override
    public void exitSequence(BarnabaParser.SequenceContext ctx) {
        StringBuilder seq = new StringBuilder();
        sequence.forEach(s -> seq.append(s));
        structureBuilder = structureBuilder.setSequence(seq.toString());
    }

    @Override
    public void enterSequenceElement(BarnabaParser.SequenceElementContext ctx) {

        int elPosition = Integer.parseInt(ctx.S_INT().getFirst().getText());
        int i = 0;

        if(nucleotidePositionMap.isEmpty()) {
            differencePosition = elPosition-1;
            lastPosition = elPosition;

            i = elPosition-differencePosition;
        }
        /**
         * Questo blocco toglie il salto presente nella sequenza
         * GGGCUGUUUUUCUCGCUGACUUUCAGCCC       CAAACAAAAAAUGUCAGCA
         * diventa:
         * GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAUGUCAGCA
         */
        else if(elPosition - lastPosition > 1) {
            differencePosition += elPosition-lastPosition-1;
            lastPosition = elPosition;
            i = elPosition-differencePosition;
        }
        else {
            lastPosition = elPosition;
            i = elPosition-differencePosition;
        }

        nucleotidePositionMap.put(elPosition, i);
        appendNucleotide(ctx.S_IUPAC_CODE().getText(), i);
    }

    private void appendNucleotide(String nucleotide, int index) {
        while (sequence.size() <= index) {
            sequence.add(" ");
        }
        sequence.set(index, nucleotide);
    }

    @Override
    public void exitSequenceElement(BarnabaParser.SequenceElementContext ctx) {

    }

    @Override
    public void enterInteraction(BarnabaParser.InteractionContext ctx) {
        currentResidue1 = null;
        currentResidue2 = null;
        currentBondType = null;

        String annotation = ctx.ANNOTATION().getText();
        if(annotation.matches("[<>][<>]")) {
            currentBondType = "stacking";
        } else {
            String pairs = annotation.substring(0, 2);
            if(pairs.equals("WC") || pairs.equals("GU")) {
                annotation = annotation.replace(pairs, "WW");
            }
            String lastChar = annotation.substring(annotation.length() - 1);
            String prefix = annotation.substring(0, annotation.length() - 1);
            currentBondType = lastChar + prefix;
        }

    }

    @Override
    public void exitInteraction(BarnabaParser.InteractionContext ctx) {
        Pair pair = new Pair(
                nucleotidePositionMap.get(Integer.parseInt(currentResidue1.INT().getFirst().getText())),
                nucleotidePositionMap.get(Integer.parseInt(currentResidue2.INT().getFirst().getText())),
                currentResidue1.NUCLEOTIDE().getText(),
                currentResidue2.NUCLEOTIDE().getText(),
                BondType.fromString(currentBondType));

        structureBuilder = structureBuilder.addPair(pair);
    }

    @Override
    public void enterResidue(BarnabaParser.ResidueContext ctx) {
        if(currentResidue1 == null) currentResidue1 = ctx;
        else currentResidue2 = ctx;
    }

    @Override
    public void exitResidue(BarnabaParser.ResidueContext ctx) {

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
