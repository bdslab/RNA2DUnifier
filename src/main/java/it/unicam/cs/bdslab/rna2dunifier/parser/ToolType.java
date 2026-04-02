package it.unicam.cs.bdslab.rna2dunifier.parser;

/**
 * Enumeration of supported RNA structure analysis tools and their output formats.
 *
 * <p>Each constant represents a specific tool whose output can be parsed
 * by the corresponding {@link RnaStructureParser} implementation.
 *
 * @author Francesco Palozzi
 * @see RnaStructureParser
 * @see ParserFactory
 */
public enum ToolType {
    /** FR3D JSON output format. */
    FR3D,

    /** RNAview output format. */
    RNAVIEW,

    /** RNApolis output format. */
    RNAPOLIS,

    /** mc‑annotate output format. */
    MCANNOTATE,

    /** Barnaba output format. */
    BARNABA,

    /** bpnet output format. */
    BPNET,

    /** x3dna JSON output format. */
    X3DNA
}