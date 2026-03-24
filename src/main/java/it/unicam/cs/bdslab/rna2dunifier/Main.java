package it.unicam.cs.bdslab.rna2dunifier;

import it.unicam.cs.bdslab.barnaba.BarnabaLexer;
import it.unicam.cs.bdslab.barnaba.BarnabaParser;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarLexer;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.fr3d.Fr3dLexer;
import it.unicam.cs.bdslab.fr3d.Fr3dParser;
import it.unicam.cs.bdslab.mcannotate.McAnnotateLexer;
import it.unicam.cs.bdslab.mcannotate.McAnnotateParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis.RNApolisParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview.RNAviewParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba.BarnabaParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.bpnet.BpnetParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.fr3d.Fr3dParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate.McAnnotateParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.x3dna.X3dnaCustomListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisLexer;
import it.unicam.cs.bdslab.rnapolis.RNApolisParser;
import it.unicam.cs.bdslab.rnaview.RNAviewLexer;
import it.unicam.cs.bdslab.rnaview.RNAviewParser;
import it.unicam.cs.bdslab.x3dna.X3dnaGrammarLexer;
import it.unicam.cs.bdslab.x3dna.X3dnaGrammarParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        CharStream cs3 = CharStreams.fromFileName("src/main/resources/rna-output/rnapolis/4PLX_A.3db");
        RNApolisLexer lexer3 = new RNApolisLexer(cs3);
        CommonTokenStream tokens3 = new CommonTokenStream(lexer3);
        RNApolisParser parser3 = new RNApolisParser(tokens3);
        ParseTree tree3 = parser3.rnapolisFile(); // parse
        RNApolisParserCustomListener listener3 = new RNApolisParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener3, tree3);
        System.out.println(listener3.getStructures().toString());

        CharStream cs = CharStreams.fromFileName("src/main/resources/rna-output/rnaview/4PLX_A.pdb.out");
        RNAviewLexer lexer = new RNAviewLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNAviewParser parser = new RNAviewParser(tokens);
        ParseTree tree = parser.rnaviewFile(); // parse
        RNAviewParserCustomListener listener = new RNAviewParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        System.out.println(listener.getStructure().toString());

        CharStream cs2 = CharStreams.fromFileName("src/main/resources/rna-output/barnaba/4PLX_A.pdb.ANNOTATE.pairing.out");
        BarnabaLexer lexer2 = new BarnabaLexer(cs2);
        CommonTokenStream tokens2 = new CommonTokenStream(lexer2);
        BarnabaParser parser2 = new BarnabaParser(tokens2);
        ParseTree tree2 = parser2.barnabaFile(); // parse
        BarnabaParserCustomListener listener2 = new BarnabaParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener2, tree2);
        System.out.println(listener2.getStructure().toString());

        CharStream cs4 = CharStreams.fromFileName("src/main/resources/rna-output/fr3d/4PLX_A_A_basepair.json");
        Fr3dLexer lexer4 = new Fr3dLexer(cs4);
        CommonTokenStream tokens4 = new CommonTokenStream(lexer4);
        Fr3dParser parser4 = new Fr3dParser(tokens4);
        ParseTree tree4 = parser4.fr3dFile(); // parse
        Fr3dParserCustomListener listener4 = new Fr3dParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener4, tree4);
        System.out.println(listener4.getStructure());

        CharStream cs5 = CharStreams.fromFileName("src/main/resources/rna-output/mc-annotate/txt/4PLX_A.txt");
        McAnnotateLexer lexer5 = new McAnnotateLexer(cs5);
        CommonTokenStream tokens5 = new CommonTokenStream(lexer5);
        McAnnotateParser parser5 = new McAnnotateParser(tokens5);
        ParseTree tree5 = parser5.mcAannotateFile(); // parse
        McAnnotateParserCustomListener listener5 = new McAnnotateParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener5, tree5);
        System.out.println(listener5.getStructure());

        CharStream cs6 = CharStreams.fromFileName("src/main/resources/rna-output/bpnet/4PLX_A.4PLX_A.out");
        BpnetGrammarLexer lexer6 = new BpnetGrammarLexer(cs6);
        CommonTokenStream tokens6 = new CommonTokenStream(lexer6);
        BpnetGrammarParser parser6 = new BpnetGrammarParser(tokens6);
        ParseTree tree6 = parser6.bpnetFile(); // parse
        BpnetParserCustomListener listener6 = new BpnetParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener6, tree6);
        System.out.println(listener6.getStructure());

        CharStream cs7 = CharStreams.fromFileName("src/main/resources/rna-output/x3dna-dssr/1YMO_A_pair-only.json");
        X3dnaGrammarLexer lexer7 = new X3dnaGrammarLexer(cs7);
        CommonTokenStream tokens7 = new CommonTokenStream(lexer7);
        X3dnaGrammarParser parser7 = new X3dnaGrammarParser(tokens7);
        ParseTree tree7 = parser7.x3dnaFile(); // parse
        X3dnaCustomListener listener7 = new X3dnaCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener7, tree7);
        System.out.println(listener7.getStructure());
    }
}