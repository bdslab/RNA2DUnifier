package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis.RNApolisCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.parser.RnaStructureParser;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarLexer;
import it.unicam.cs.bdslab.rnapolis.RNApolisGrammarParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 *
 */
public class RnapolisParser implements RnaStructureParser {
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR Stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        RNApolisGrammarLexer lexer = new RNApolisGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNApolisGrammarParser parser = new RNApolisGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.rnapolisFile();

        // Listener build the structure
        RNApolisCustomListener listener = new RNApolisCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return Secondary Structure
        return listener.getStructures().getFirst();
    }
}
