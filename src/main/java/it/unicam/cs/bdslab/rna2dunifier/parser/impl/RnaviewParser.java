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
 * Parser implementation for RNAview output files.
 *
 * <p>This parser uses the ANTLR-generated lexer and parser for the RNAview grammar.
 * It reads an input stream, walks the parse tree with a {@link RNAviewCustomListener},
 * and builds an {@link ExtendedRNASecondaryStructure} object.
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see RNAviewCustomListener
 */
public class RnaviewParser implements RnaStructureParser {

    /**
     * Parses an RNAview output file from the given input stream.
     *
     * @param inputStream the input stream containing the RNAview file content
     * @return an {@link ExtendedRNASecondaryStructure} representing the parsed data
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the RNAview grammar
     */
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        RNAviewGrammarLexer lexer = new RNAviewGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNAviewGrammarParser parser = new RNAviewGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.rnaviewFile();

        // Listener builds the structure
        RNAviewCustomListener listener = new RNAviewCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return secondary structure
        return listener.getStructure();
    }
}