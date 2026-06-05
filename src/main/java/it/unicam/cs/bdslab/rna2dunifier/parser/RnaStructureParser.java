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

package it.unicam.cs.bdslab.rna2dunifier.parser;

import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Parser interface for RNA secondary structure files.
 *
 * <p>Implementations of this interface are responsible for parsing
 * various input formats (e.g., bpseq, barnaba, mc‑annotate, JSON, etc.)
 * and producing a uniform {@link ExtendedRNASecondaryStructure} object.
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 */
public interface RnaStructureParser {
    /**
     * Parses an input stream containing an RNA secondary structure description
     * and returns the corresponding model object.
     *
     * @param inputStream the input stream to read from (not closed by the parser)
     * @return an {@link ExtendedRNASecondaryStructure} representing the parsed data
     * @throws IOException    if an I/O error occurs while reading the stream
     * @throws ParseException if the input cannot be parsed according to the expected format
     */
    ExtendedRNASecondaryStructure parse(InputStream inputStream) throws IOException, ParseException;
}
