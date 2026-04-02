package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarLexer;
import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate.McAnnotateCustomListener;
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
public class McAnnotateParser implements RnaStructureParser {
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR Stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        McAnnotateGrammarLexer lexer = new McAnnotateGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        McAnnotateGrammarParser parser = new McAnnotateGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.mcAnnotateFile();

        // Listener build the structure
        McAnnotateCustomListener listener = new McAnnotateCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return Secondary Structure
        return listener.getStructure();
    }
}
