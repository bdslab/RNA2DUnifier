package it.unicam.cs.bdslab.rna2dunifier;

import it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis.RNApolisParserCustomListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisLexer;
import it.unicam.cs.bdslab.rnapolis.RNApolisParser;
import it.unicam.cs.bdslab.rnapolis.RNApolisParserListener;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String input = """
                >strand_A
                seq GGAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUCUUAAAAAAAAAAAAAGCAAAA
                cWW .[[[[[..........((((((((....))))))))(((((((((((..]]]]]...........)))))))))))
                cWH ......E({BFDA<C[.................................................e)}bfd.a>c]
                cSW ..................................................(............)............
                tSW .....(..........................................................)...........
                tSH ........................(.).................................................
                """;
        CharStream cs = CharStreams.fromString(input);
        RNApolisLexer lexer = new RNApolisLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNApolisParser parser = new RNApolisParser(tokens);
        ParseTree tree = parser.rnapolisFile(); // parse
        RNApolisParserCustomListener listener = new RNApolisParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        System.out.println(listener.getStructures().toString());
    }
}