package it.unicam.cs.bdslab.rna2dunifier.listeners.fr3d;

import it.unicam.cs.bdslab.JSON.JSONListener;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.stream.Collectors;

public class JSONFr3dListener implements JSONListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;
    private Pair.Builder pairBuilder;
    private ExtendedRNASecondaryStructure structure;

    private final Stack<String> positionStack = new Stack<>();
    private boolean inAnnotations = false;

    private final Set<Integer> positions = new HashSet<>();
    private final Map<Integer, Integer> positionMap = new HashMap<>();


    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    @Override
    public void enterJson(JSONParser.JsonContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    @Override
    public void exitJson(JSONParser.JsonContext ctx) {
        this.structure = structureBuilder.build();
    }

    @Override
    public void enterValue(JSONParser.ValueContext ctx) {

    }

    @Override
    public void exitValue(JSONParser.ValueContext ctx) {

    }

    @Override
    public void enterObject(JSONParser.ObjectContext ctx) {
        if(inAnnotations) {
            pairBuilder = new Pair.Builder();
        }
    }

    @Override
    public void exitObject(JSONParser.ObjectContext ctx) {
        if(inAnnotations) {
            structureBuilder.addPair(pairBuilder.build());
        }
    }

    @Override
    public void enterMember(JSONParser.MemberContext ctx) {
        String memberName = ctx.STRING().getText().replaceAll("\"", "");
        positionStack.push(memberName);
        if(inAnnotations) {
            buildPair(ctx.STRING().getText().replaceAll("\"", ""), ctx);
        } else {
            switch (memberName) {
                case "pdb_id":
                    structureBuilder.addHeaderInfo("PDB ID", ctx.value().STRING().getText());
                    break;
                case "chain_id":
                    structureBuilder.addHeaderInfo("Chain ID", ctx.value().STRING().getText());
                    break;
                case "annotations":
                    enterAnnotations(ctx);
                    break;
                case "modified":
                    addToPositions(ctx.value().array().value().stream()
                            .map(JSONParser.ValueContext::object)
                            .collect(Collectors.toList())
                    );
                    break;
            }
        }
    }

    private void enterAnnotations(JSONParser.MemberContext ctx) {
        inAnnotations = true;
        addToPositions(ctx.value().array().value().stream().map(JSONParser.ValueContext::object).collect(Collectors.toList()));

        List<Integer> sortedPositions = new ArrayList<>(positions);
        Collections.sort(sortedPositions);

        int i = 0;
        for(Integer position : sortedPositions) {
            positionMap.put(position, i++);
        }
    }

    private void addToPositions(List<JSONParser.ObjectContext> objects) {
        objects.forEach(obj ->
                obj.member().stream()
                        .filter(s ->
                            s.STRING().getText().contains("seq_id1") ||
                            s.STRING().getText().contains("seq_id2") ||
                            s.STRING().getText().contains("seq_id"))
                        .forEach(s ->
                        positions.add(Integer.parseInt(s.value().STRING().getText().replaceAll("\"", ""))))
        );
    }

    private void buildPair(String item, JSONParser.MemberContext ctx) {

        String val = ctx.value().STRING().getText().replaceAll("\"", "");

        switch (item) {
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

    @Override
    public void exitMember(JSONParser.MemberContext ctx) {
        positionStack.pop();
        if(positionStack.isEmpty()) {
            inAnnotations = false;
        }
    }

    @Override
    public void enterArray(JSONParser.ArrayContext ctx) {

    }

    @Override
    public void exitArray(JSONParser.ArrayContext ctx) {

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
