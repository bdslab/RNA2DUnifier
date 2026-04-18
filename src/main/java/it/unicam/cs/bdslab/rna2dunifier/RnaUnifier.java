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
 * <p>This class provides static methods to parse an RNA structure file produced
 * by any of the supported tools (FR3D, RNAview, RNApolis, mc‑annotate, Barnaba,
 * bpnet, x3dna) and convert it into a unified bpseq format.
 *
 * <p>The unification process consists of:
 * <ol>
 *   <li>Selecting the appropriate parser based on the tool type</li>
 *   <li>Parsing the input into an {@link ExtendedRNASecondaryStructure}</li>
 *   <li>Exporting the structure as a bpseq string using {@link BpseqExporter}</li>
 * </ol>
 *
 * @author Francesco Palozzi
 * @see ToolType
 * @see RnaStructureParser
 * @see BpseqExporter
 */
public class RnaUnifier {

    /**
     * Parses an input file and returns its unified bpseq representation.
     *
     * @param inputFile the input file containing the RNA structure description
     * @param toolType  the tool that generated the input file (determines the parser)
     * @return a bpseq string representation of the RNA secondary structure
     * @throws IOException    if an I/O error occurs while reading the file
     * @throws ParseException if the input file does not conform to the expected format
     */
    public static String process(File inputFile, ToolType toolType) throws IOException, ParseException {
        try (InputStream is = new FileInputStream(inputFile)) {
            return process(is, toolType);
        }
    }

    /**
     * Parses an input file, auto-detecting the source tool, and returns
     * its unified bpseq representation.
     *
     * @param inputFile the input file to process
     * @return a bpseq string representation of the RNA secondary structure
     * @throws IOException              if an I/O error occurs
     * @throws ParseException           if the format is not valid
     * @throws IllegalArgumentException if the tool type cannot be detected
     */
    public static String process(File inputFile) throws IOException, ParseException {
        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(inputFile))) {
            ToolType type = ParserFactory.detectTool(bis);
            System.out.println(type);
            return process(bis, type);
        }
    }

    /**
     * Parses an input stream and returns its unified bpseq representation.
     *
     * @param inputStream the input stream containing the RNA structure description
     * @param toolType    the tool that generated the input (determines the parser)
     * @return a bpseq string representation of the RNA secondary structure
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input does not conform to the expected format
     */
    public static String process(InputStream inputStream, ToolType toolType) throws IOException, ParseException {
        RnaStructureParser parser = ParserFactory.getParser(toolType);
        ExtendedRNASecondaryStructure structure = parser.parse(inputStream);
        return BpseqExporter.export(structure);
    }

    /**
     * Parses an input stream, auto-detecting the source tool, and returns
     * its unified bpseq representation.
     *
     * <p>The stream <b>must</b> support mark/reset — wrap in a
     * {@link java.io.BufferedInputStream} if it does not.
     *
     * @param inputStream the stream to process (must support mark/reset)
     * @return a bpseq string representation
     * @throws IOException              if an I/O error occurs
     * @throws ParseException           if the format is not valid
     * @throws IllegalArgumentException if the tool type cannot be detected
     */
    public static String process(InputStream inputStream) throws IOException, ParseException {
        ToolType type = ParserFactory.detectTool(inputStream);
        System.out.println(type);
        return process(inputStream, type);
    }

    /**
     * Parses an input file and writes the unified bpseq representation directly to an output file.
     *
     * @param inputFile  the input file containing the RNA structure description
     * @param toolType   the tool that generated the input file (determines the parser)
     * @param outputFile the file where the bpseq output will be written
     * @throws IOException    if an I/O error occurs while reading the input or writing the output
     * @throws ParseException if the input file does not conform to the expected format
     */
    public static void processToFile(File inputFile, ToolType toolType, File outputFile) throws IOException, ParseException {
        String bpseq = process(inputFile, toolType);
        Files.write(outputFile.toPath(), bpseq.getBytes());
    }

    /**
     * Parses an input file, auto-detecting the source tool, and writes
     * the bpseq output to the specified file.
     */
    public static void processToFile(File inputFile, File outputFile) throws IOException, ParseException {
        String bpseq = process(inputFile);
        Files.write(outputFile.toPath(), bpseq.getBytes());
    }
}