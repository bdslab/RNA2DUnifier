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
 * Parser implementation for Barnaba output files.
 *
 * <p>This parser uses the ANTLR-generated lexer and parser for the Barnaba grammar.
 * It reads an input stream, walks the parse tree with a {@link BarnabaCustomListener},
 * and builds an {@link ExtendedRNASecondaryStructure} object.
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see BarnabaCustomListener
 */
public class BarnabaParser implements RnaStructureParser {

    /**
     * Parses a Barnaba output file from the given input stream.
     *
     * @param inputStream the input stream containing the Barnaba file content
     * @return an {@link ExtendedRNASecondaryStructure} representing the parsed data
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the Barnaba grammar
     */
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

        // Listener builds the structure
        BarnabaCustomListener listener = new BarnabaCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return secondary structure
        return listener.getStructure();
    }
}