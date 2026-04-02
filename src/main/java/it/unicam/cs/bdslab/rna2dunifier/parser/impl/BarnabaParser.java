package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.barnaba.BarnabaGrammarLexer;
import it.unicam.cs.bdslab.barnaba.BarnabaGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba.BarnabaCustomListener;
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
public class BarnabaParser implements RnaStructureParser {
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        BarnabaGrammarLexer lexer = new BarnabaGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BarnabaGrammarParser parser = new BarnabaGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.barnabaFile();

        // Listener build the structure
        BarnabaCustomListener listener = new BarnabaCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return Secondary Structure
        return listener.getStructure();
    }
}
