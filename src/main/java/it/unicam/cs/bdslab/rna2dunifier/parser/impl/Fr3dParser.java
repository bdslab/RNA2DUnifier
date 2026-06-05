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

import it.unicam.cs.bdslab.JSON.JSONLexer;
import it.unicam.cs.bdslab.JSON.JSONParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.fr3d.JSONFr3dListener;
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
 * Parser implementation for FR3D JSON output files.
 *
 * <p>This parser uses the ANTLR-generated lexer and parser for the JSON grammar
 * (specifically adapted for FR3D output). It reads an input stream, walks the parse tree
 * with a {@link JSONFr3dListener}, and builds an {@link ExtendedRNASecondaryStructure} object.
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see JSONFr3dListener
 */
public class Fr3dParser implements RnaStructureParser {

    /**
     * Parses an FR3D JSON file from the given input stream.
     *
     * @param inputStream the input stream containing the FR3D JSON content
     * @return an {@link ExtendedRNASecondaryStructure} representing the parsed data
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the expected JSON structure
     */
    @Override
    public ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException {
        // Create ANTLR stream
        CharStream charStream = CharStreams.fromStream(inputStream);

        // Lexer and parser from ANTLR
        JSONLexer lexer = new JSONLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JSONParser parser = new JSONParser(tokens);

        // Parsing
        ParseTree tree = parser.json();

        // Listener builds the structure
        JSONFr3dListener listener = new JSONFr3dListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Return secondary structure
        return listener.getStructure();
    }
}
