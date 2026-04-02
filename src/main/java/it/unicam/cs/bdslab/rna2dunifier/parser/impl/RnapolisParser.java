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
 * Parser implementation for RNApolis output files.
 *
 * <p>This parser uses the ANTLR-generated lexer and parser for the RNApolis grammar.
 * It reads an input stream, walks the parse tree with a {@link RNApolisCustomListener},
 * and builds a list of {@link ExtendedRNASecondaryStructure} objects (one per strand).
 * The method {@link #parse(InputStream)} returns the first structure in the list.
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see RNApolisCustomListener
 */
public class RnapolisParser implements RnaStructureParser {

    /**
     * Parses an RNApolis file from the given input stream and returns the first
     * secondary structure (i.e., the first strand section).
     *
     * @param inputStream the input stream containing the RNApolis file content
     * @return an {@link ExtendedRNASecondaryStructure} representing the first strand
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the RNApolis grammar
     */
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        RNApolisGrammarLexer lexer = new RNApolisGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNApolisGrammarParser parser = new RNApolisGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.rnapolisFile();

        // Listener builds the structure
        RNApolisCustomListener listener = new RNApolisCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return secondary structure (first strand)
        return listener.getStructures().getFirst();
    }
}