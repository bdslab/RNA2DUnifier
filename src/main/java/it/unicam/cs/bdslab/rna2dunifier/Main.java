package it.unicam.cs.bdslab.rna2dunifier;

import it.unicam.cs.bdslab.JSON.JSONLexer;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.barnaba.BarnabaGrammarLexer;
import it.unicam.cs.bdslab.barnaba.BarnabaGrammarParser;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarLexer;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarLexer;
import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis.RNApolisCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview.RNAviewCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba.BarnabaCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.bpnet.BpnetParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.fr3d.JSONFr3dListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate.McAnnotateCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.x3dna.JSONX3dnaListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarLexer;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarParser;
import it.unicam.cs.bdslab.rnaview.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        CharStream cs6 = CharStreams.fromFileName("src/main/resources/rna-output/bpnet/4PLX_A.4PLX_A.out");
        BpnetGrammarLexer lexer6 = new BpnetGrammarLexer(cs6);
        CommonTokenStream tokens6 = new CommonTokenStream(lexer6);
        BpnetGrammarParser parser6 = new BpnetGrammarParser(tokens6);
        ParseTree tree6 = parser6.bpnetFile(); // parse
        BpnetParserCustomListener listener6 = new BpnetParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener6, tree6);
        System.out.println(listener6.getStructure());

        CharStream cs8 = CharStreams.fromFileName("src/main/resources/rna-output/x3dna-dssr/1YMO_A.json");
        JSONLexer lexer8 = new JSONLexer(cs8);
        CommonTokenStream tokens8 = new CommonTokenStream(lexer8);
        JSONParser parser8 = new JSONParser(tokens8);
        ParseTree tree8 = parser8.json(); // parse
        JSONX3dnaListener listener8 = new JSONX3dnaListener();
        ParseTreeWalker.DEFAULT.walk(listener8, tree8);
        System.out.println(listener8.getStructure());

        CharStream cs7 = CharStreams.fromFileName("src/main/resources/rna-output/fr3d/4PLX_C_C_basepair.json");
        JSONLexer lexer7 = new JSONLexer(cs7);
        CommonTokenStream tokens7 = new CommonTokenStream(lexer7);
        JSONParser parser7 = new JSONParser(tokens7);
        ParseTree tree7 = parser7.json(); // parse
        JSONFr3dListener listener7 = new JSONFr3dListener();
        ParseTreeWalker.DEFAULT.walk(listener7, tree7);
        System.out.println(listener7.getStructure());

        CharStream cs1 = CharStreams.fromFileName("src/main/resources/rna-output/rnapolis/4PLX_A.3db");
        RNApolisGrammarLexer lexer1 = new RNApolisGrammarLexer(cs1);
        CommonTokenStream tokens1 = new CommonTokenStream(lexer1);
        RNApolisGrammarParser parser1 = new RNApolisGrammarParser(tokens1);
        ParseTree tree1 = parser1.rnapolisFile(); // parse
        RNApolisCustomListener listener1 = new RNApolisCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener1, tree1);
        System.out.println(listener1.getStructures().toString());

        CharStream cs3 = CharStreams.fromFileName("src/main/resources/rna-output/rnaview/2M8K_A.pdb.out");
        RNAviewGrammarLexer lexer3 = new RNAviewGrammarLexer(cs3);
        CommonTokenStream tokens3 = new CommonTokenStream(lexer3);
        RNAviewGrammarParser parser3 = new RNAviewGrammarParser(tokens3);
        ParseTree tree3 = parser3.rnaviewFile(); // parse
        RNAviewCustomListener listener3 = new RNAviewCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener3, tree3);
        System.out.println(listener3.getStructure().toString());

        CharStream cs = CharStreams.fromFileName("src/main/resources/rna-output/barnaba/4PLX_C.pdb.ANNOTATE.stacking.out");
        BarnabaGrammarLexer lexer = new BarnabaGrammarLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BarnabaGrammarParser parser = new BarnabaGrammarParser(tokens);
        ParseTree tree = parser.barnabaFile(); // parse
        BarnabaCustomListener listener = new BarnabaCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        System.out.println(listener.getStructure().toString());

        CharStream cs2 = CharStreams.fromFileName("src/main/resources/rna-output/mc-annotate/txt/2M8K_A.txt");
        McAnnotateGrammarLexer lexer2 = new McAnnotateGrammarLexer(cs2);
        CommonTokenStream tokens2 = new CommonTokenStream(lexer2);
        McAnnotateGrammarParser parser2 = new McAnnotateGrammarParser(tokens2);
        ParseTree tree2 = parser2.mcAnnotateFile(); // parse
        McAnnotateCustomListener listener2 = new McAnnotateCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener2, tree2);
        System.out.println(listener2.getStructure());
    }
}