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

package it.unicam.cs.bdslab.rna2dunifier.listeners.mcAnnotate;

import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarBaseListener;
import it.unicam.cs.bdslab.mcannotate.McAnnotateGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import java.util.HashMap;
import java.util.Map;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom ANTLR listener for parsing mc‑annotate output files.
 *
 * <p>This listener processes mc‑annotate grammar parse events to build an
 * {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Residue section – reconstructs the RNA sequence and builds a position map</li>
 *   <li>Non‑adjacent stacking lines – creates stacking interactions</li>
 *   <li>Base‑pair lines – creates base‑pair interactions with proper bond types</li>
 * </ul>
 * The listener maps original residue numbers to zero‑based indices and
 * reconstructs the sequence from the residue lines.
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class McAnnotateCustomListener extends McAnnotateGrammarBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(McAnnotateCustomListener.class);

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** StringBuilder for efficient sequence accumulation. */
    private final StringBuilder sequenceBuilder = new StringBuilder();

    /** Maps original residue numbers (from PDB) to zero‑based indices. */
    private final Map<Integer, Integer> positionMap = new HashMap<>();

    /**
     * Returns the parsed RNA secondary structure.
     *
     * @return the built {@link ExtendedRNASecondaryStructure}
     */
    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    /**
     * Called when entering the root {@code mcAnnotateFile} rule.
     * Initialises the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterMcAnnotateFile(McAnnotateGrammarParser.McAnnotateFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
        if (logger.isDebugEnabled()) logger.debug("Started parsing mc‑annotate file");
    }

    /**
     * Called when exiting the root {@code mcAnnotateFile} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitMcAnnotateFile(McAnnotateGrammarParser.McAnnotateFileContext ctx) {
        this.structure = structureBuilder.build();
        logger.info("Sequence length: {}, total pairs: {}", sequenceBuilder.length(), structure.getPairs().size());
    }

    // ----------------------------------------------------------------------
    // Residue section – build sequence and position map
    // ----------------------------------------------------------------------

    /**
     * Called when entering the {@code residueSection} rule.
     * Initialises the sequence accumulator and tracking variables.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterResidueSection(McAnnotateGrammarParser.ResidueSectionContext ctx) {
        sequenceBuilder.setLength(0);
        positionMap.clear();
        if (logger.isDebugEnabled()) logger.debug("Entering residue section");
    }

    /**
     * Called when exiting the {@code residueSection} rule.
     * Sets the reconstructed sequence in the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitResidueSection(McAnnotateGrammarParser.ResidueSectionContext ctx) {
        structureBuilder.setSequence(sequenceBuilder.toString());
        logger.info("Reconstructed sequence of length {}", sequenceBuilder.length());
    }

    /**
     * Called when entering a {@code residueLine} rule.
     * Extracts the nucleotide and position, appends the nucleotide to the sequence,
     * and updates the position map.
     * <p>
     * The first {@code IDENTIFIER} contains the residue identifier (e.g., "A1"),
     * the second {@code IDENTIFIER} contains the nucleotide type.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterResidueLine(McAnnotateGrammarParser.ResidueLineContext ctx) {
        String residueId = ctx.IDENTIFIER(0).getText(); // e.g., "A1"
        String nucleotideToken = ctx.IDENTIFIER(1).getText(); // e.g., "A" or "ADE"
        String nucleotide = normalizeNucleotide(nucleotideToken);

        sequenceBuilder.append(nucleotide);

        // Extract original residue number (e.g., "A1" → 1)
        int originalNumber = parseResidueNumber(residueId);

        if (originalNumber > 0) {
            int index = sequenceBuilder.length() - 1;
            positionMap.put(originalNumber, index);
            if (logger.isTraceEnabled()) logger.trace("Mapped {} → index {}", originalNumber, index);
        } else {
            logger.warn("Could not parse residue number from '{}'", residueId);
        }
    }

    private String normalizeNucleotide(String token) {
        if (token == null || token.isEmpty()) {
            return "N";
        }
        if (token.length() == 1) {
            return token;
        }
        if (logger.isDebugEnabled()) logger.debug("Unknown nucleotide token '{}' → 'N'", token);
        return "N";
    }

    private int parseResidueNumber(String residueId) {
        if (residueId == null || residueId.length() < 2) {
            logger.warn("Invalid residue identifier: '{}'", residueId);
            return -1;
        }
        try {
            return Integer.parseInt(residueId.substring(1));
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse residue number from '{}'", residueId);
            return -1;
        }
    }

    // ----------------------------------------------------------------------
    // Stacking and base-pair lines – build pairs
    // ----------------------------------------------------------------------

    /**
     * Called when entering a {@code nonAdjacentLine} rule.
     * Creates a stacking pair (bond type "stacking") and adds it to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterNonAdjacentLine(McAnnotateGrammarParser.NonAdjacentLineContext ctx) {
        String pairId = ctx.PAIR_ID().getText();
        Pair p = buildPair(pairId, BondType.STACKING);
        structureBuilder.addPair(p);
    }

    /**
     * Called when entering a {@code basePairLine} rule.
     * Creates a base‑pair interaction with the appropriate bond type and adds it to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterBasePairLine(McAnnotateGrammarParser.BasePairLineContext ctx) {
        String pairId = ctx.PAIR_ID().getText();
        BondType bondType = getBondType(ctx.ORIENTATION(), ctx.BOND().getFirst().getText());
        Pair p = buildPair(pairId, bondType);
        structureBuilder.addPair(p);
    }

    /**
     * Converts an orientation and bond string into an internal {@link BondType}.
     * <p>
     * The bond format is {@code edge1/edge2} (e.g., "W/W"). Orientation is either
     * "cis" (converted to 'c') or "trans" (converted to 't').
     *
     * @param orientation the orientation token (may be null)
     * @param bond        the bond string (e.g., "W/W")
     * @return the corresponding {@code BondType}, or {@link BondType#UNKNOWN} if orientation is missing
     */
    private BondType getBondType(TerminalNode orientation, String bond) {
        if (orientation == null || orientation.getText().isEmpty()) {
            logger.warn("Missing orientation for bond '{}' –> UNKNOWN", bond);
            return BondType.UNKNOWN;
        }

        String prefix = orientation.getText().equals("cis") ? "c" : "t";

        String cleanBond = bond.split("\\s+")[0];
        String[] edges = cleanBond.split("/");

        if (edges.length < 2) {
            logger.warn("Malformed bond string '{}' – UNKNOWN", bond);
            return BondType.UNKNOWN;
        }

        String edge1 = edges[0].isEmpty() ? "?" : edges[0].substring(0, 1);
        String edge2 = edges[1].isEmpty() ? "?" : edges[1].substring(0, 1);

        return BondType.fromString(prefix + edge1 + edge2);
    }

    /**
     * Builds a {@link Pair} object from a pair identifier string and a bond type.
     * <p>
     * The pair identifier format is {@code A1-U2}, where the first part is the
     * first residue and the second part is the second residue. Residue numbers
     * are extracted from the identifiers (e.g., "A1" → position 1) and then
     * mapped through {@code positionMap} to zero‑based indices.
     * Nucleotides are retrieved from the reconstructed sequence.
     *
     * @param pos      the pair identifier (e.g., "A1-U2")
     * @param bondType the type of interaction
     * @return a new {@code Pair} with resolved positions and nucleotides
     */
    private Pair buildPair(String pos, BondType bondType) {
        String[] parts = pos.split("-");
        if (parts.length != 2) {
            logger.warn("Invalid pair identifier '{}' – cannot build pair", pos);
            return null;
        }

        int orig1 = parseResidueNumber(parts[0]);
        int orig2 = parseResidueNumber(parts[1]);

        Integer zero1 = positionMap.get(orig1);
        Integer zero2 = positionMap.get(orig2);
        if (zero1 == null || zero2 == null) {
            logger.warn("Residue numbers not found in position map: {} or {} – skipping pair", orig1, orig2);
            return null;
        }

        String seq = sequenceBuilder.toString();
        if (zero1 >= seq.length() || zero2 >= seq.length()) {
            logger.warn("Index out of bounds: {} or {} (seq length {})", zero1, zero2, seq.length());
            return null;
        }

        String nt1 = String.valueOf(seq.charAt(zero1));
        String nt2 = String.valueOf(seq.charAt(zero2));

        if (logger.isTraceEnabled()) logger.trace(
            "Built pair: {}-{} ({}-{}) type {}",
            zero1,
            zero2,
            nt1,
            nt2,
            bondType
        );
        return new Pair(zero1, zero2, nt1, nt2, bondType);
    }
}
