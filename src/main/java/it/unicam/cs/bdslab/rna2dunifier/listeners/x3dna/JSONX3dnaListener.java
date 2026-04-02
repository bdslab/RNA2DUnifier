package it.unicam.cs.bdslab.rna2dunifier.listeners.x3dna;

import it.unicam.cs.bdslab.JSON.JSONBaseListener;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;

import java.util.Stack;

public class JSONX3dnaListener extends JSONBaseListener {

    private ExtendedRNASecondaryStructure.Builder structureBuilder;
    private Pair.Builder pairBuilder;
    private ExtendedRNASecondaryStructure structure;

    private final Stack<String> positionStack = new Stack<>();
    private boolean inPairs = false;

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
    public void enterObject(JSONParser.ObjectContext ctx) {
        if(inPairs) {
            pairBuilder = new Pair.Builder();
        }
    }

    @Override
    public void exitObject(JSONParser.ObjectContext ctx) {
        if(inPairs) {
            structureBuilder.addPair(pairBuilder.build());
        }
    }

    @Override
    public void enterMember(JSONParser.MemberContext ctx) {
        String val = ctx.STRING().getText().replaceAll("\"", "");
        buildPair(val, ctx);
        positionStack.push(val);
        if(positionStack.size() == 1 && positionStack.peek().equals("pairs")) {
            inPairs = true;
        }
    }

    private void buildPair(String val, JSONParser.MemberContext ctx) {
        if(inPairs) {
            String item;
            switch (val) {
                case "nt1":
                    item = getItem(ctx);
                    pairBuilder.setPos1(Integer.parseInt(item.substring(3)));
                    pairBuilder.setNucleotide1(item.substring(2,3));
                    break;
                case "nt2":
                    item = getItem(ctx);
                    pairBuilder.setPos2(Integer.parseInt(item.substring(3)));
                    pairBuilder.setNucleotide2(item.substring(2,3));
                    break;
                case "LW":
                    item = getItem(ctx);
                    pairBuilder.setType(BondType.fromString(item));
                    break;
            }
        }
    }

    private String getItem(JSONParser.MemberContext ctx) {
        return ctx.value().STRING().getText().replaceAll("\"", "");
    }

    @Override
    public void exitMember(JSONParser.MemberContext ctx) {
        positionStack.pop();
        if(positionStack.isEmpty()) {
            inPairs = false;
        }
    }
}
