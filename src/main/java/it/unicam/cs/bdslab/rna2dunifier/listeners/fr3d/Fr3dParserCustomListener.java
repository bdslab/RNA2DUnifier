package it.unicam.cs.bdslab.rna2dunifier.listeners.fr3d;

import it.unicam.cs.bdslab.fr3d.Fr3dListener;
import it.unicam.cs.bdslab.fr3d.Fr3dParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class Fr3dParserCustomListener implements Fr3dListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    private Pair.Builder pairBuilder;

    private boolean inAnnotations = false;

    Map<Integer, Integer> positionMap = new HashMap<>();

    public ExtendedRNASecondaryStructure getStructure() {
        return structureBuilder.build();
    }

    @Override
    public void enterFr3dFile(Fr3dParser.Fr3dFileContext ctx) {
    }

    @Override
    public void exitFr3dFile(Fr3dParser.Fr3dFileContext ctx) {
    }

    @Override
    public void enterPdb_id(Fr3dParser.Pdb_idContext ctx) {
        structureBuilder.addHeaderInfo("PDB ID", ctx.string_pair().String().getLast().getText());
    }

    @Override
    public void exitPdb_id(Fr3dParser.Pdb_idContext ctx) {
    }

    @Override
    public void enterChain_id(Fr3dParser.Chain_idContext ctx) {
        structureBuilder.addHeaderInfo("Chain ID", ctx.string_pair().String().getLast().getText());
    }

    @Override
    public void exitChain_id(Fr3dParser.Chain_idContext ctx) {
    }

    @Override
    public void enterModified(Fr3dParser.ModifiedContext ctx) {
    }

    @Override
    public void exitModified(Fr3dParser.ModifiedContext ctx) {
    }

    @Override
    public void enterAnnotations(Fr3dParser.AnnotationsContext ctx) {
        inAnnotations = true;
        Set<Integer> positions = new HashSet<>();

        ctx.object().forEach(obj -> {
            obj.string_pair()
                    .stream()
                    .filter(s ->
                            s.String().getFirst().getText().contains("seq_id1") ||
                            s.String().getFirst().getText().contains("seq_id2"))
                    .forEach(s -> {
                        positions.add(Integer.parseInt(s.String().getLast().getText().replace("\"", "")));
                    });
        });

        List<Integer> sortedPositions = new ArrayList<>(positions);
        Collections.sort(sortedPositions);

        int i = 0;
        for(Integer position : sortedPositions) {
            positionMap.put(position, i++);
        }
    }

    @Override
    public void exitAnnotations(Fr3dParser.AnnotationsContext ctx) {
        inAnnotations = false;
    }

    @Override
    public void enterObject(Fr3dParser.ObjectContext ctx) {
        if(inAnnotations) {
            pairBuilder = new Pair.Builder();
        }
    }

    @Override
    public void exitObject(Fr3dParser.ObjectContext ctx) {
        if(inAnnotations) {
            structureBuilder.addPair(pairBuilder.build());
        }
    }

    @Override
    public void enterString_pair(Fr3dParser.String_pairContext ctx) {
        if(inAnnotations) {

            String val = getVal(ctx);

            switch (ctx.String().getFirst().getText().replace("\"", "")) {
                case "seq_id1":
                    pairBuilder.setPos1(positionMap.get(Integer.parseInt(val)));
                    break;

                case "seq_id2":
                    pairBuilder.setPos2(positionMap.get(Integer.parseInt(val)));
                    break;

                case "nt1":
                    pairBuilder.setNucleotide1(val);
                    break;

                case "nt2":
                    pairBuilder.setNucleotide2(val);
                    break;

                case "bp":
                    pairBuilder.setType(BondType.fromString(val));
                    break;
            }
        }
    }

    private String getVal(Fr3dParser.String_pairContext ctx) {
        return ctx.String().getLast().getText().replace("\"", "");
    }

    @Override
    public void exitString_pair(Fr3dParser.String_pairContext ctx) {

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
