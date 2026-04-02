package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.JSON.JSONLexer;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.x3dna.JSONX3dnaListener;
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
public class X3dnaParser implements RnaStructureParser {
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR Stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        JSONLexer lexer = new JSONLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JSONParser parser = new JSONParser(tokens);

        // Parsing
        ParseTree tree = parser.json();

        // Listener build the structure
        JSONX3dnaListener listener = new JSONX3dnaListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return Secondary Structure
        return listener.getStructure();
    }
}
