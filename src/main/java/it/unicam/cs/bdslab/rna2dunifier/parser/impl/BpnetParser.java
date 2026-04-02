package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.bpnet.BpnetGrammarLexer;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.bpnet.BpnetParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.parser.RnaStructureParser;
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
public class BpnetParser implements RnaStructureParser {
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        BpnetGrammarLexer lexer = new BpnetGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BpnetGrammarParser parser = new BpnetGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.bpnetFile();

        // Listener build the structure
        BpnetParserCustomListener listener = new BpnetParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return Secondary Structure
        return listener.getStructure();
    }
}
