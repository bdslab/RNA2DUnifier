package it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarBaseListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarParser;

import java.util.*;

public class RNApolisCustomListener extends RNApolisGrammarBaseListener {

    private final List<ExtendedRNASecondaryStructure> structures = new ArrayList<>();
    private ExtendedRNASecondaryStructure.Builder currentStructureBuilder;
    private BondType currentInteractionType;
    private String currentSequence;

    public List<ExtendedRNASecondaryStructure> getStructures() {
        return structures;
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
    public void enterSequence(RNApolisGrammarParser.SequenceContext ctx) {
        this.currentSequence = ctx.NUCLEOTIDE_SEQUENCE().getText();
        this.currentStructureBuilder.setSequence(this.currentSequence);
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
}
