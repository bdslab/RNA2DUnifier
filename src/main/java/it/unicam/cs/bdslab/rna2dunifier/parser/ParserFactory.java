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

import it.unicam.cs.bdslab.rna2dunifier.parser.impl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
     * Automatically detects the tool type by scanning the first kilobytes
     * of the input stream for format-specific signatures.
     *
     * <p>The detection heuristics, derived from the ANTLR grammars, are:
     * <ol>
     *   <li><b>RNAview</b>  – contains the literal {@code BEGIN_base-pair}</li>
     *   <li><b>McAnnotate</b> – contains {@code Residue conformations}</li>
     *   <li><b>RNApolis</b>  – contains a FASTA-style header ({@code >…}) followed by {@code seq}</li>
     *   <li><b>FR3D</b>      – JSON containing the key {@code "annotations"}</li>
     *   <li><b>X3DNA</b>     – JSON containing the key {@code "pairs"}</li>
     *   <li><b>Barnaba</b>   – lines matching {@code NINT_INT NINT_INT ANNOTATION} (e.g. {@code A_1_2 G_3_4 WWc})</li>
     *   <li><b>Bpnet</b>     – lines containing a {@code ?} separator and a bond token like {@code W:WC}</li>
     * </ol>
     *
     * <p><b>Important:</b> the stream must support {@link InputStream#mark(int)} /
     * {@link InputStream#reset()} (wrap it in a {@link java.io.BufferedInputStream}
     * if it does not). The stream position is restored before returning so the
     * caller can still parse normally.
     *
     * @param inputStream the stream to inspect (must support mark/reset)
     * @return the detected {@link ToolType}
     * @throws IOException                if reading fails
     * @throws IllegalArgumentException   if the format cannot be recognised
     */
    public static ToolType detectTool(InputStream inputStream) throws IOException {
        final int PEEK_SIZE = 4096;
        inputStream.mark(PEEK_SIZE);
        byte[] buf = inputStream.readNBytes(PEEK_SIZE);
        inputStream.reset();

        String preview = new String(buf, StandardCharsets.UTF_8);

        if (preview.contains("BEGIN_base-pair")) return ToolType.RNAVIEW;
        if (preview.contains("Residue conformations")) return ToolType.MCANNOTATE;
        if (preview.contains(">") && preview.contains("seq ")) return ToolType.RNAPOLIS;

        // JSON formats: distinguish by dominant key
        if (preview.trim().startsWith("{") || preview.trim().startsWith("[")) {
            if (preview.contains("\"annotations\"")) return ToolType.FR3D;
            if (preview.contains("\"pairs\"")) return ToolType.X3DNA;
        }

        // Barnaba: NUCLEOTIDE_INT_INT pairs + WWc/WCc-style annotation
        if (
            preview.matches("(?s).*[ACGUacgu]_\\d+_\\d+\\s+[ACGUacgu]_\\d+_\\d+\\s+[GUWCHS]{2}[ct].*")
        ) return ToolType.BARNABA;

        // Bpnet: lines with '?' separator and W:W bond notation
        if (
            preview.contains("?") && preview.matches("(?s).*\\d+\\s+\\d+\\s+\\w+\\s+\\?.*[WSHE]:[WSHE][CT].*")
        ) return ToolType.BPNET;

        throw new IllegalArgumentException("Unable to detect the tool type from the provided input.");
    }
}
