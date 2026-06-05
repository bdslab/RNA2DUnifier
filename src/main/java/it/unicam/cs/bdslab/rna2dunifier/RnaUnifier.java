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

package it.unicam.cs.bdslab.rna2dunifier;

import it.unicam.cs.bdslab.rna2dunifier.exporter.BpseqExporter;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.parser.ParserFactory;
import it.unicam.cs.bdslab.rna2dunifier.parser.RnaStructureParser;
import it.unicam.cs.bdslab.rna2dunifier.parser.ToolType;
import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;

/**
 * Main entry point for the RNA secondary structure unification pipeline.
 *
 * <p>This class provides methods to parse an RNA structure file produced
 * by any of the supported tools (FR3D, RNAview, RNApolis, mc‑annotate, Barnaba,
 * bpnet, x3dna) and convert it into a unified <strong>extended bpseq</strong> format.
 *
 * <p>The extended bpseq format augments the standard bpseq with additional columns
 * that list pairing partners for each Leontis‑Westhof bond type at every position.
 *
 * <p>The unification process consists of:
 * <ol>
 *   <li>Selecting the appropriate parser based on the tool type (or auto‑detecting it)</li>
 *   <li>Parsing the input into an {@link ExtendedRNASecondaryStructure}</li>
 *   <li>Exporting the structure as an extended bpseq string using {@link BpseqExporter#printExtendedBPSEQ}</li>
 * </ol>
 *
 * @author Francesco Palozzi
 * @see ToolType
 * @see RnaStructureParser
 * @see BpseqExporter
 */
public class RnaUnifier {

    private final BpseqExporter exporter;

    public RnaUnifier() {
        exporter = new BpseqExporter();
    }

    public RnaUnifier(BpseqExporter exporter) {
        this.exporter = exporter;
    }

    /**
     * Parses an input file and returns its unified extended bpseq representation.
     *
     * @param inputFile the input file containing the RNA structure description
     * @param toolType  the tool that generated the input file (determines the parser)
     * @return an extended bpseq string representation of the RNA secondary structure
     * @throws IOException    if an I/O error occurs while reading the file
     * @throws ParseException if the input file does not conform to the expected format
     */
    public String process(File inputFile, ToolType toolType, boolean extended) throws IOException, ParseException {
        try (InputStream is = new FileInputStream(inputFile)) {
            return process(is, toolType, extended);
        }
    }

    /**
     * Parses an input file, auto-detecting the source tool, and returns
     * its unified extended bpseq representation.
     *
     * @param inputFile the input file to process
     * @return an extended bpseq string representation of the RNA secondary structure
     * @throws IOException              if an I/O error occurs
     * @throws ParseException           if the format is not valid
     * @throws IllegalArgumentException if the tool type cannot be detected
     */
    public String process(File inputFile, boolean extended) throws IOException, ParseException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile))) {
            return process(bis, ParserFactory.detectTool(bis), extended);
        }
    }

    /**
     * Parses an input stream and returns its unified extended bpseq representation.
     *
     * @param inputStream the input stream containing the RNA structure description
     * @param toolType    the tool that generated the input (determines the parser)
     * @return an extended bpseq string representation of the RNA secondary structure
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the expected format
     */
    public String process(InputStream inputStream, ToolType toolType, boolean extended)
        throws IOException, ParseException {
        RnaStructureParser parser = ParserFactory.getParser(toolType);
        ExtendedRNASecondaryStructure structure = parser.parse(inputStream);
        return extended ? this.exporter.printExtendedBPSEQ(structure) : this.exporter.printCanonicalBPSEQ(structure);
    }

    /**
     * Parses an input stream, auto-detecting the source tool, and returns
     * its unified extended bpseq representation.
     *
     * <p>The stream <b>must</b> support mark/reset — wrap in a
     * {@link java.io.BufferedInputStream} if it does not.
     *
     * @param inputStream the stream to process (must support mark/reset)
     * @return an extended bpseq string representation
     * @throws IOException              if an I/O error occurs
     * @throws ParseException           if the format is not valid
     * @throws IllegalArgumentException if the tool type cannot be detected
     */
    public String process(InputStream inputStream, boolean extended) throws IOException, ParseException {
        ToolType type = ParserFactory.detectTool(inputStream);
        return process(inputStream, type, extended);
    }

    /**
     * Parses an input file and writes the unified extended bpseq representation
     * directly to an output file.
     *
     * @param inputFile  the input file containing the RNA structure description
     * @param toolType   the tool that generated the input file (determines the parser)
     * @param outputFile the file where the extended bpseq output will be written
     * @throws IOException    if an I/O error occurs while reading the input or writing the output
     * @throws ParseException if the input file does not conform to the expected format
     */
    public void processToFile(File inputFile, ToolType toolType, File outputFile, boolean extended)
        throws IOException, ParseException {
        String bpseq = process(inputFile, toolType, extended);
        Files.write(outputFile.toPath(), bpseq.getBytes());
    }

    /**
     * Parses an input file, auto-detecting the source tool, and writes
     * the unified extended bpseq output to the specified file.
     *
     * @param inputFile  the input file to process (tool type will be auto-detected)
     * @param outputFile the file where the extended bpseq output will be written
     * @throws IOException              if an I/O error occurs
     * @throws ParseException           if the input format is not valid
     * @throws IllegalArgumentException if the tool type cannot be detected
     */
    public void processToFile(File inputFile, File outputFile, boolean extended) throws IOException, ParseException {
        String bpseq = process(inputFile, extended);
        Files.write(outputFile.toPath(), bpseq.getBytes());
    }
}
