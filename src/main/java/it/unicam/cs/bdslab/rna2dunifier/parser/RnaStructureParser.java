package it.unicam.cs.bdslab.rna2dunifier.parser;

import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 *
 */
public interface RnaStructureParser {
    /**
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @throws ParseException
     */
    ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException;
}
