package it.unicam.cs.bdslab.rna2dunifier;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String input = "(1+2)*3-4";
        CharStream cs = CharStreams.fromString(input);
        CalcLexer lexer = new CalcLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CalcParser parser = new CalcParser(tokens);
        ParseTree tree = parser.start(); // regola iniziale
        System.out.println(tree.toStringTree(parser));
    }
}