package it.unicam.cs.bdslab.rna2dunifier.parser;

import it.unicam.cs.bdslab.rna2dunifier.parser.impl.*;

/**
 *
 */
public class ParserFactory {

    /**
     *
     * @param type
     * @return
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
}
