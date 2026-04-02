package it.unicam.cs.bdslab.rna2dunifier;

import it.unicam.cs.bdslab.rna2dunifier.exporter.BpseqExporter;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.parser.ParserFactory;
import it.unicam.cs.bdslab.rna2dunifier.parser.RnaStructureParser;
import it.unicam.cs.bdslab.rna2dunifier.parser.ToolType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.ParseException;

/**
 *
 */
public class RnaUnifier {

    /**
     *
     * @param inputFile
     * @param toolType
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static String process(File inputFile, ToolType toolType) throws IOException, ParseException {
        try (InputStream is = new FileInputStream(inputFile)) {
            return process(is, toolType);
        }
    }

    /**
     *
     * @param inputStream
     * @param toolType
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static String process(InputStream inputStream, ToolType toolType) throws IOException, ParseException {
        RnaStructureParser parser = ParserFactory.getParser(toolType);
        ExtendedRNASecondaryStructure structure = parser.parse(inputStream);
        return BpseqExporter.export(structure);
    }

    /**
     *
     * @param inputFile
     * @param toolType
     * @param outputFile
     * @throws IOException
     * @throws ParseException
     */
    public static void processToFile(File inputFile, ToolType toolType, File outputFile) throws IOException, ParseException {
        String bpseq = process(inputFile, toolType);
        Files.write(outputFile.toPath(), bpseq.getBytes());
    }
}
