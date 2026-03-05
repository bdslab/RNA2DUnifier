package it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnapolis.RNApolisParser;
import it.unicam.cs.bdslab.rnapolis.RNApolisParserListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class RNApolisParserCustomListener implements RNApolisParserListener {

    private final List<ExtendedRNASecondaryStructure> structures = new ArrayList<>();
    private ExtendedRNASecondaryStructure.Builder currentBuilder;
    private String currentInteractionType;
    private String currentSequence;

    public List<ExtendedRNASecondaryStructure> getStructures() {
        return structures;
    }

    @Override
    public void enterRnapolisFile(RNApolisParser.RnapolisFileContext ctx) {

    }

    @Override
    public void exitRnapolisFile(RNApolisParser.RnapolisFileContext ctx) {

    }

    /**
     * Enter a new Strand Section. Reset the builder
     * @param ctx the parse tree
     */
    @Override
    public void enterStrandSection(RNApolisParser.StrandSectionContext ctx) {
        currentBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitStrandSection(RNApolisParser.StrandSectionContext ctx) {
        structures.add(currentBuilder.build());
    }

    /**
     * Enter a strand name and save the name to the builder
     * @param ctx the parse tree
     */
    @Override
    public void enterStrandName(RNApolisParser.StrandNameContext ctx) {
        String name = ctx.TITLE().getText();
        currentBuilder = currentBuilder.addHeaderInfo("strand_name", name);
    }

    @Override
    public void exitStrandName(RNApolisParser.StrandNameContext ctx) {

    }

    /**
     * Enter a sequence line. Save the sequence line to the builder
     * @param ctx the parse tree
     */
    @Override
    public void enterSequenceLine(RNApolisParser.SequenceLineContext ctx) {
        currentSequence = ctx.NUCLEOTIDE().getText();
        currentBuilder = currentBuilder.setSequence(currentSequence);
    }

    @Override
    public void exitSequenceLine(RNApolisParser.SequenceLineContext ctx) {

    }

    @Override
    public void enterInteractionLine(RNApolisParser.InteractionLineContext ctx) {
        currentInteractionType = ctx.INTERACTION_TYPE().getText();
    }

    @Override
    public void exitInteractionLine(RNApolisParser.InteractionLineContext ctx) {

    }

    /**
     * Enter the interaction pattern. Add the pairs to the builder
     * @param ctx the parse tree
     */
    @Override
    public void enterInteractionPattern(RNApolisParser.InteractionPatternContext ctx) {
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
        for (int i = 0; i < ctx.SYMBOL().size(); i++) {
            char symbol = ctx.SYMBOL(i).getText().charAt(0);

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
                    currentBuilder = currentBuilder.addPair(
                            new Pair(openPos, i,
                                    String.valueOf(currentSequence.charAt(openPos)),
                                    String.valueOf(currentSequence.charAt(i)),
                                    BondType.fromString(currentInteractionType))
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
    public void exitInteractionPattern(RNApolisParser.InteractionPatternContext ctx) {

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
