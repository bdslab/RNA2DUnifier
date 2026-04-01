package it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class RNApolisCustomListener implements RNApolisGrammarListener {

    private final List<ExtendedRNASecondaryStructure> structures = new ArrayList<>();
    private ExtendedRNASecondaryStructure.Builder currentStructureBuilder;
    private BondType currentInteractionType;
    private String currentSequence;

    public List<ExtendedRNASecondaryStructure> getStructures() {
        return structures;
    }

    @Override
    public void enterRnapolisFile(RNApolisGrammarParser.RnapolisFileContext ctx) {

    }

    @Override
    public void exitRnapolisFile(RNApolisGrammarParser.RnapolisFileContext ctx) {

    }

    @Override
    public void enterStrandSection(RNApolisGrammarParser.StrandSectionContext ctx) {
        this.currentStructureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitStrandSection(RNApolisGrammarParser.StrandSectionContext ctx) {
        this.structures.add(this.currentStructureBuilder.build());
    }

    @Override
    public void enterHeader(RNApolisGrammarParser.HeaderContext ctx) {
        this.currentStructureBuilder.addHeaderInfo("strand_name", ctx.HEADER_STRING().getText().substring(1));
    }

    @Override
    public void exitHeader(RNApolisGrammarParser.HeaderContext ctx) {

    }

    @Override
    public void enterSequence(RNApolisGrammarParser.SequenceContext ctx) {
        this.currentSequence = ctx.NUCLEOTIDE_SEQUENCE().getText();
        this.currentStructureBuilder.setSequence(this.currentSequence);
    }

    @Override
    public void exitSequence(RNApolisGrammarParser.SequenceContext ctx) {

    }

    @Override
    public void enterInteraction(RNApolisGrammarParser.InteractionContext ctx) {
        this.currentInteractionType = BondType.fromString(ctx.INTERACTION_TYPE().getText());

        buildPairs(ctx.INTERACTION_SEQUENCE().getText());
    }

    private void buildPairs(String interactionSequence) {
        // Map open -> close
        Map<Character, Character> openToClose = new HashMap<>();
        openToClose.put('(', ')');
        openToClose.put('[', ']');
        openToClose.put('{', '}');
        openToClose.put('<', '>');
        for (char c = 'A'; c <= 'Z'; c++) {
            openToClose.put(c, Character.toLowerCase(c));
        }

        // Map close -> open (fast lookup)
        Map<Character, Character> closeToOpen = new HashMap<>();
        for (Map.Entry<Character, Character> e : openToClose.entrySet()) {
            closeToOpen.put(e.getValue(), e.getKey());
        }

        // Open symbols stacks
        Map<Character, Stack<Integer>> stacks = new HashMap<>();

        // Iteration pattern
        for (int i = 0; i < interactionSequence.length(); i++) {
            char symbol = interactionSequence.charAt(i);

            if (openToClose.containsKey(symbol)) {
                // push to corresponding stack
                stacks.computeIfAbsent(symbol, k -> new Stack<>()).push(i);
            }
            else if (closeToOpen.containsKey(symbol)) {
                // find opening symbol
                char openChar = closeToOpen.get(symbol);
                Stack<Integer> stack = stacks.get(openChar);
                if (stack != null && !stack.isEmpty()) {
                    int openPos = stack.pop(); // last opening position
                    // create pair
                    this.currentStructureBuilder.addPair(
                            new Pair(openPos, i, // 0-index
                                    String.valueOf(currentSequence.charAt(openPos)),
                                    String.valueOf(currentSequence.charAt(i)),
                                    currentInteractionType)
                    );
                } else {
                    // No opening found ERROR!
                    System.err.println("Closing without opening: " + symbol + " at position " + i);
                }
            }
        }

        // Check opening not closed
        for (Map.Entry<Character, Stack<Integer>> e : stacks.entrySet()) {
            if (!e.getValue().isEmpty()) {
                System.err.println("Symbol " + e.getKey() + " has " + e.getValue().size() + " opening not closed");
            }
        }
    }

    @Override
    public void exitInteraction(RNApolisGrammarParser.InteractionContext ctx) {

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
