package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview.RNAviewCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.parser.RnaStructureParser;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarLexer;
import it.unicam.cs.bdslab.rnaview.RNAviewGrammarParser;
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
public class RnaviewParser implements RnaStructureParser {
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR Stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        RNAviewGrammarLexer lexer = new RNAviewGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNAviewGrammarParser parser = new RNAviewGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.rnaviewFile();

        // Listener build the structure
        RNAviewCustomListener listener = new RNAviewCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return Secondary Structure
        return listener.getStructure();
    }
}
