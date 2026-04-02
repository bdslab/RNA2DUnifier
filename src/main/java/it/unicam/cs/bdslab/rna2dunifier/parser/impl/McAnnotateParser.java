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
 * Parser implementation for mc‑annotate output files.
 *
 * <p>This parser uses the ANTLR-generated lexer and parser for the mc‑annotate grammar.
 * It reads an input stream, walks the parse tree with a {@link McAnnotateCustomListener},
 * and builds an {@link ExtendedRNASecondaryStructure} object.
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see McAnnotateCustomListener
 */
public class McAnnotateParser implements RnaStructureParser {

    /**
     * Parses a mc‑annotate output file from the given input stream.
     *
     * @param inputStream the input stream containing the mc‑annotate file content
     * @return an {@link ExtendedRNASecondaryStructure} representing the parsed data
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the mc‑annotate grammar
     */
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        McAnnotateGrammarLexer lexer = new McAnnotateGrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        McAnnotateGrammarParser parser = new McAnnotateGrammarParser(tokens);

        // Parsing
        ParseTree tree = parser.mcAnnotateFile();

        // Listener builds the structure
        McAnnotateCustomListener listener = new McAnnotateCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return secondary structure
        return listener.getStructure();
    }
}