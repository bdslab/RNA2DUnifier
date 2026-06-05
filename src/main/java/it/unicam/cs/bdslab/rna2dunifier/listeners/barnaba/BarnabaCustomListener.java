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

package it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba;

import it.unicam.cs.bdslab.barnaba.BarnabaGrammarBaseListener;
import it.unicam.cs.bdslab.barnaba.BarnabaGrammarParser;
import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom ANTLR listener for parsing Barnaba output files.
 *
 * <p>This listener processes Barnaba grammar parse events to build an
 * {@link ExtendedRNASecondaryStructure} object. It handles:
 * <ul>
 *   <li>Residue specifications (nucleotide type and position mapping)</li>
 *   <li>Interaction lines (base‑pair and stacking annotations)</li>
 *   <li>Comment lines containing sequence information, PDB file names,
 *       and skipping directives for uncommon residues</li>
 * </ul>
 * The listener reconstructs the complete RNA sequence (inserting 'N' for
 * skipped residues) and builds a list of base‑pair interactions with
 * appropriate bond types.
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see BondType
 */
public class BarnabaCustomListener extends BarnabaGrammarBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(BarnabaCustomListener.class);

    /** Builder for the final RNA secondary structure. */
    private ExtendedRNASecondaryStructure.Builder structureBuilder;

    /** Builder for the current base pair being processed. */
    private Pair.Builder pairBuilder;

    /** The final built structure. */
    private ExtendedRNASecondaryStructure structure;

    /** Maps original PDB residue numbers to zero‑based indices in the reconstructed sequence. */
    private final Map<Integer, Integer> nucleotidePositionMap = new HashMap<>();

    /** StringBuilder for the sequence being built. */
    private final StringBuilder sequenceBuilder = new StringBuilder();

    /** Number of uncommon residues skipped so far. */
    private int uncommonResidues = 0;

    /** Flag indicating whether the first nucleotide of the current pair has been set. */
    private boolean nt1Viewed = false;

    /**
     * Returns the parsed RNA secondary structure.
     *
     * @return the built {@link ExtendedRNASecondaryStructure}
     */
    public ExtendedRNASecondaryStructure getStructure() {
        return structure;
    }

    /**
     * Called when entering the root {@code barnabaFile} rule.
     * Initialises the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterBarnabaFile(BarnabaGrammarParser.BarnabaFileContext ctx) {
        this.structureBuilder = new ExtendedRNASecondaryStructure.Builder();
    }

    /**
     * Called when exiting the root {@code barnabaFile} rule.
     * Builds the final structure.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitBarnabaFile(BarnabaGrammarParser.BarnabaFileContext ctx) {
        if (uncommonResidues > 0) {
            logger.info("Total uncommon residues skipped: {}", uncommonResidues);
        }
        this.structure = structureBuilder.build();
    }

    /**
     * Called when entering a {@code commentLine} rule.
     * Processes three types of comments:
     * <ul>
     *   <li>"Skipping" – increments the counter of uncommon residues</li>
     *   <li>"sequence" – reconstructs the full RNA sequence, filling gaps with 'N'</li>
     *   <li>"PDB" – stores the PDB file name as header information</li>
     * </ul>
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterCommentLine(BarnabaGrammarParser.CommentLineContext ctx) {
        String comment = ctx.COMMENT().getText().replace("#", "").trim();
        String[] parts = comment.split(" ");
        if (parts.length == 0) return;

        switch (parts[0]) {
            case "Skipping":
                uncommonResidues++;
                logger.warn("{} (no placeholder inserted)", comment);
                break;
            case "sequence":
                if (parts.length > 1) {
                    String seqRaw = parts[1];
                    Arrays.stream(seqRaw.split("-")).forEach(this::processSequenceElement);
                    structureBuilder.setSequence(sequenceBuilder.toString());
                }
                break;
            case "PDB":
                if (parts.length > 1) {
                    structureBuilder.addHeaderInfo("File name", parts[1]);
                }
                break;
        }
    }

    private void processSequenceElement(String element) {
        String[] fields = element.split("_");
        if (fields.length != 3) {
            logger.warn("Malformed sequence element: {}", element);
            return;
        }
        String nucleotide = fields[0];
        int pdbNumber = Integer.parseInt(fields[1]);
        int currentIndex = sequenceBuilder.length();
        nucleotidePositionMap.put(pdbNumber, currentIndex);
        sequenceBuilder.append(nucleotide);
    }

    /**
     * Called when entering an {@code interactionLine} rule.
     * Creates a new {@link Pair.Builder} and sets its bond type from the annotation.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterInteractionLine(BarnabaGrammarParser.InteractionLineContext ctx) {
        this.pairBuilder = new Pair.Builder();
        this.pairBuilder.setType(AnnotationMapper.toBondType(ctx.ANNOTATION().getText()));
        this.nt1Viewed = false;
    }

    /**
     * Called when exiting an {@code interactionLine} rule.
     * Adds the completed pair to the structure builder.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void exitInteractionLine(BarnabaGrammarParser.InteractionLineContext ctx) {
        Pair pair = pairBuilder.build();
        if (!shouldSkipPair(pair)) {
            structureBuilder.addPair(pair);
        }
    }

    /**
     * Called when entering a {@code residueSpec} rule (a single nucleotide specification).
     * Sets either the first or second nucleotide of the current pair based on the {@code nt1Viewed} flag.
     *
     * @param ctx the parse tree context
     */
    @Override
    public void enterResidueSpec(BarnabaGrammarParser.ResidueSpecContext ctx) {
        int pdbNumber = Integer.parseInt(ctx.INT().getFirst().getText());
        Integer index = nucleotidePositionMap.get(pdbNumber);
        if (index == null) {
            logger.warn("Residue {} not found in sequence mapping. Skipping pair.", pdbNumber);
            return;
        }
        if (!nt1Viewed) {
            this.pairBuilder.setNucleotide1(ctx.NUCLEOTIDE().getText());
            this.pairBuilder.setPos1(index);
            nt1Viewed = true;
        } else {
            this.pairBuilder.setNucleotide2(ctx.NUCLEOTIDE().getText());
            this.pairBuilder.setPos2(index);
            nt1Viewed = false;
        }
    }

    private boolean shouldSkipPair(Pair pair) {
        // Skip only adjacent stacking interactions
        return Math.abs(pair.getPos1() - pair.getPos2()) == 1 && pair.getType() == BondType.STACKING;
    }

    // ----------------------------------------------------------------------
    // Helper class for annotation conversion
    // ----------------------------------------------------------------------
    private static final class AnnotationMapper {

        private static final Map<String, BondType> ANNOTATION_MAP = new HashMap<>();

        static {
            // Stacking annotations
            ANNOTATION_MAP.put(">>", BondType.STACKING);
            ANNOTATION_MAP.put("<<", BondType.STACKING);
            ANNOTATION_MAP.put("<>", BondType.STACKING);
            ANNOTATION_MAP.put("><", BondType.STACKING);
            // Canonical and wobble pairs
            ANNOTATION_MAP.put("WCc", BondType.fromString("cWW"));
            ANNOTATION_MAP.put("GUc", BondType.fromString("cWW"));
        }

        static BondType toBondType(String annotation) {
            // Check predefined mappings
            BondType bt = ANNOTATION_MAP.get(annotation);
            if (bt != null) {
                return bt;
            }
            // Handle dynamic patterns like "WHc", "HSt", etc.
            if (annotation.matches("[WHS][WHS][ct]")) {
                String edges = annotation.substring(0, 2);
                String orientation = annotation.substring(2);
                // "WHc" -> "cWH"
                String transformed = orientation + edges;
                BondType dynamicType = BondType.fromString(transformed);
                if (dynamicType != BondType.UNKNOWN) {
                    return dynamicType;
                }
            }
            // Unknown annotation
            logger.warn("Unknown annotation: '{}' – mapped to UNKNOWN", annotation);
            return BondType.UNKNOWN;
        }
    }
}
