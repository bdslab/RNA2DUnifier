package it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba;

import it.unicam.cs.bdslab.barnaba.BarnabaGrammarListener;
import it.unicam.cs.bdslab.barnaba.BarnabaGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class BarnabaCustomListener implements BarnabaGrammarListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;
    private Pair.Builder pairBuilder;
    private ExtendedRNASecondaryStructure structure;

    private final Map<Integer, Integer> nucleotidePositionMap = new HashMap<>();
    private final List<String> sequence = new ArrayList<>();
    private int uncommonResidues = 0;
    private int lastPosition = 0;
    private int differencePosition = 0;

    private boolean nt1Viewed = false;

    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    @Override
    public void enterBarnabaFile(BarnabaGrammarParser.BarnabaFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitBarnabaFile(BarnabaGrammarParser.BarnabaFileContext ctx) {
        this.structure = structureBuilder.build();
    }

    @Override
    public void enterResidueSpec(BarnabaGrammarParser.ResidueSpecContext ctx) {
        int pos = nucleotidePositionMap.get(Integer.parseInt(ctx.INT().getFirst().getText()));
        if(!nt1Viewed) {
            nt1Viewed = true;
            this.pairBuilder.setNucleotide1(ctx.NUCLEOTIDE().getText());
            this.pairBuilder.setPos1(pos);
        } else {
            nt1Viewed = false;
            this.pairBuilder.setNucleotide2(ctx.NUCLEOTIDE().getText());
            this.pairBuilder.setPos2(pos);
        }
    }

    @Override
    public void exitResidueSpec(BarnabaGrammarParser.ResidueSpecContext ctx) {

    }

    @Override
    public void enterInteractionLine(BarnabaGrammarParser.InteractionLineContext ctx) {
        this.pairBuilder = new Pair.Builder();
        this.pairBuilder.setType(getBondType(ctx.ANNOTATION().getText()));
    }

    private BondType getBondType(String annotation) {
        if(annotation.matches("[<>][<>]")) {
            return BondType.fromString("stacking");
        } else {
            String pairs = annotation.substring(0, 2);
            if(pairs.equals("WC") || pairs.equals("GU")) {
                // WWc pairs between complementary bases are called WCc or GUc.
                annotation = annotation.replace(pairs, "WW");
            }
            String lastChar = annotation.substring(annotation.length() - 1);
            String prefix = annotation.substring(0, annotation.length() - 1);
            return BondType.fromString(lastChar + prefix);
        }
    }

    @Override
    public void exitInteractionLine(BarnabaGrammarParser.InteractionLineContext ctx) {
        this.structureBuilder.addPair(pairBuilder.build());
    }

    @Override
    public void enterCommentLine(BarnabaGrammarParser.CommentLineContext ctx) {
        String comment = ctx.COMMENT().getText().replace("#", "").trim();
        String[] splittedComment = comment.split(" ");

        switch (splittedComment[0]) {
            case "Skipping":
                uncommonResidues++;
                break;
            case "sequence":
                String sequenceRaw = splittedComment[1];
                Arrays.stream(sequenceRaw.split("-")).forEach(this::enterSequenceElement);
                StringBuilder seq = new StringBuilder();
                sequence.forEach(seq::append);
                while (uncommonResidues-- > 0) {
                    // Fill position of uncommon residue
                    seq.append("N");
                }
                structureBuilder = structureBuilder.setSequence(seq.toString());
                break;
            case "PDB":
                this.structureBuilder.addHeaderInfo("File name", splittedComment[1]);
                break;
        }
    }

    private void enterSequenceElement(String element) {
        String[] elements = element.split("_");

        String nucleotide = elements[0];

        int elPosition = Integer.parseInt(elements[1]);
        int i = 0;

        if(nucleotidePositionMap.isEmpty()) {
            // Fill position of uncommon residue
            int gapStartPosition = elPosition - 1;
            if(elPosition > 1) {
                while (gapStartPosition > 0 && uncommonResidues > 0) {
                    gapStartPosition--;
                    uncommonResidues--;
                    nucleotidePositionMap.put(i, i); // 0-index
                    appendNucleotide("N", i++);
                }
            }
            differencePosition = gapStartPosition+1;
            lastPosition = elPosition;
        }
        else if(elPosition - lastPosition > 1) {

            // Fill position of uncommon residue
            int difference = elPosition - lastPosition;
            while (difference > 1 && uncommonResidues > 0) {

                difference--;
            }

            /*
             * Remove the jump in the sequence
             * from: GGGCUGUUUUUCUCGCUGACUUUCAGCCC       CAAACAAAAAAUGUCAGCA
             * to:   GGGCUGUUUUUCUCGCUGACUUUCAGCCCCAAACAAAAAAUGUCAGCA
             */
            differencePosition += elPosition-lastPosition-1;
            lastPosition = elPosition;
            i = elPosition-differencePosition;
        }
        else {
            lastPosition = elPosition;
            i = elPosition-differencePosition;
        }

        nucleotidePositionMap.put(elPosition, i); // 0-index
        appendNucleotide(nucleotide, i);
    }

    private void appendNucleotide(String nucleotide, int index) {
        while (sequence.size() < index) {
            sequence.add(" ");
        }
        sequence.add(nucleotide);
    }

    @Override
    public void exitCommentLine(BarnabaGrammarParser.CommentLineContext ctx) {

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
