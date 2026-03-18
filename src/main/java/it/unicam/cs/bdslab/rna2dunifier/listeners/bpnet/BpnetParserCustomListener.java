package it.unicam.cs.bdslab.rna2dunifier.listeners.bpnet;

import it.unicam.cs.bdslab.bpnet.BpnetGrammarListener;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashSet;
import java.util.Set;

public class BpnetParserCustomListener implements BpnetGrammarListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    private final Set<Pair> pairs = new HashSet<>();

    private final StringBuilder sequence = new StringBuilder();

    private int currentPosition;
    private String currentNucleotide;

    public ExtendedRNASecondaryStructure getStructure() {
        return structureBuilder.build();
    }

    @Override
    public void enterBpnetFile(BpnetGrammarParser.BpnetFileContext ctx) {
        structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitBpnetFile(BpnetGrammarParser.BpnetFileContext ctx) {
        structureBuilder.setSequence(sequence.toString());
        pairs.forEach(pair -> structureBuilder.addPair(pair));
    }

    @Override
    public void enterPairs(BpnetGrammarParser.PairsContext ctx) {
        currentPosition = Integer.parseInt(ctx.INT().getFirst().getText());
        currentNucleotide = String.valueOf(ctx.TEXT().getFirst().getText().charAt(0));

        sequence.append(currentNucleotide);
    }

    @Override
    public void exitPairs(BpnetGrammarParser.PairsContext ctx) {

    }

    @Override
    public void enterPair(BpnetGrammarParser.PairContext ctx) {
        pairs.add(new Pair(
                currentPosition-1,
                Integer.parseInt(ctx.INT().getFirst().getText())-1,
                currentNucleotide,
                ctx.TEXT().getFirst().getText(),
                getType(ctx.BOND().getText()))
        );
    }

    private BondType getType(String bond) {
        String edge1 = convertEdge(bond.substring(0,1));
        String edge2 = convertEdge(bond.substring(2,3));
        String orientation = bond.substring(3).toLowerCase();

        return BondType.fromString(orientation+edge1+edge2);
    }

    private String convertEdge(String edge) {
        if(edge.toLowerCase().matches("[whs]")) return edge.toUpperCase();
        return switch (edge) {
            case "+" -> "W";
            case "z" -> "S";
            case "g" -> "H";
            default -> "?";
        };
    }

    @Override
    public void exitPair(BpnetGrammarParser.PairContext ctx) {

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
