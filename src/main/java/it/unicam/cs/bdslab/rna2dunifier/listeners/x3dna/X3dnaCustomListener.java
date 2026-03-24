package it.unicam.cs.bdslab.rna2dunifier.listeners.x3dna;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.x3dna.X3dnaGrammarListener;
import it.unicam.cs.bdslab.x3dna.X3dnaGrammarParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Stack;

public class X3dnaCustomListener implements X3dnaGrammarListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    private ExtendedRNASecondaryStructure structure;

    private final Stack<String> positionStack = new Stack<>();
    private boolean inPairs = false;

    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    @Override
    public void enterX3dnaFile(X3dnaGrammarParser.X3dnaFileContext ctx) {
        structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitX3dnaFile(X3dnaGrammarParser.X3dnaFileContext ctx) {
        structure = structureBuilder.build();
    }

    @Override
    public void enterItem(X3dnaGrammarParser.ItemContext ctx) {
        if(ctx.String() != null) {
            positionStack.push(ctx.String().getText());
        } else {
            positionStack.push(ctx.string_pair() != null ?
                    ctx.string_pair().String().getFirst().getText() :
                    ctx.number_pair().String().getText());
        }

        if(positionStack.size() == 1 && positionStack.peek().equals("\"pairs\"")) {
            inPairs = true;
        }
    }

    @Override
    public void exitItem(X3dnaGrammarParser.ItemContext ctx) {
        positionStack.pop();
        if(inPairs) inPairs = false;
    }

    @Override
    public void enterObject(X3dnaGrammarParser.ObjectContext ctx) {
        if(inPairs) {
            structureBuilder.addPair(buildPair(ctx.string_pair()));
        }
    }

    private Pair buildPair(List<X3dnaGrammarParser.String_pairContext> string_pairs) {
        int pos1 = 0;
        int pos2 = 0;
        String nt1 = null;
        String nt2 = null;
        BondType type = null;

        for(X3dnaGrammarParser.String_pairContext pair : string_pairs) {
            String val = pair.String().getLast().getText().replaceAll("\"", "");
            switch (pair.String().getFirst().getText().replaceAll("\"", "")) {
                case "nt1":
                    pos1 = Integer.parseInt(val.substring(3));
                    nt1 = val.substring(2,3);
                    break;
                case "nt2":
                    pos2 = Integer.parseInt(val.substring(3));
                    nt2 = val.substring(2,3);
                    break;
                case "LW":
                    type = BondType.fromString(val);
                    break;
            }
        }

        return new Pair(pos1, pos2, nt1, nt2, type);
    }

    @Override
    public void exitObject(X3dnaGrammarParser.ObjectContext ctx) {

    }

    @Override
    public void enterArray(X3dnaGrammarParser.ArrayContext ctx) {

    }

    @Override
    public void exitArray(X3dnaGrammarParser.ArrayContext ctx) {

    }

    @Override
    public void enterString_pair(X3dnaGrammarParser.String_pairContext ctx) {

    }

    @Override
    public void exitString_pair(X3dnaGrammarParser.String_pairContext ctx) {

    }

    @Override
    public void enterNumber_pair(X3dnaGrammarParser.Number_pairContext ctx) {

    }

    @Override
    public void exitNumber_pair(X3dnaGrammarParser.Number_pairContext ctx) {

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
