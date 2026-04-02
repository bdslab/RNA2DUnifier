package it.unicam.cs.bdslab.rna2dunifier.parser;

import it.unicam.cs.bdslab.rna2dunifier.parser.impl.*;

/**
 * Factory class for obtaining RNA structure parsers based on the tool type.
 *
 * <p>This factory provides a convenient way to instantiate the appropriate
 * parser implementation for a given output format (e.g., FR3D, RNAview, RNApolis,
 * mc‑annotate, Barnaba, bpnet, x3dna).
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see ToolType
 */
public class ParserFactory {

    /**
     * Returns a parser instance for the specified tool type.
     *
     * @param type the tool type (format) for which a parser is requested
     * @return an implementation of {@link RnaStructureParser} capable of parsing
     *         files produced by the given tool
     */
    public static RnaStructureParser getParser(ToolType type) {
        return switch (type) {
            case FR3D -> new Fr3dParser();
            case RNAVIEW -> new RnaviewParser();
            case RNAPOLIS -> new RnapolisParser();
            case MCANNOTATE -> new McAnnotateParser();
            case BARNABA -> new BarnabaParser();
            case BPNET -> new BpnetParser();
            case X3DNA -> new X3dnaParser();
        };
    }

    /**
     * Automatically detects the tool type from the input file content.
     *
     * <p>This method is intended to analyse the structure of an input file
     * (e.g., header lines, keywords, format patterns) and infer which tool
     * produced it, eliminating the need for manual specification of
     * {@link ToolType}.
     *
     * @return the detected {@link ToolType} corresponding to the input format
     * @throws UnsupportedOperationException if this feature is not yet implemented
     */
    public static ToolType detectTool() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}