/*
 * Copyright 2026 Francesco Palozzi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unicam.cs.bdslab.rna2dunifier.parser.impl;

import it.unicam.cs.bdslab.bpnet.BpnetGrammarLexer;
import it.unicam.cs.bdslab.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.bpnet.BpnetParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.parser.RnaStructureParser;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Parser implementation for bpnet output files.
 *
 * <p>This parser uses the ANTLR-generated lexer and parser for the bpnet grammar.
 * It reads an input stream, walks the parse tree with a {@link BpnetParserCustomListener},
 * and builds an {@link ExtendedRNASecondaryStructure} object.
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see BpnetParserCustomListener
 */
public class BpnetParser implements RnaStructureParser {

    /**
     * Parses a bpnet output file from the given input stream.
     *
     * @param inputStream the input stream containing the bpnet file content
     * @return an {@link ExtendedRNASecondaryStructure} representing the parsed data
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the bpnet grammar
     */
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

        // Listener builds the structure
        BpnetParserCustomListener listener = new BpnetParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return secondary structure
        return listener.getStructure();
    }
}
