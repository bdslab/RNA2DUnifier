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
    X3DNA,
}
