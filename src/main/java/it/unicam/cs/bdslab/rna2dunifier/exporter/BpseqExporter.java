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

package it.unicam.cs.bdslab.rna2dunifier.exporter;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exporter for converting an {@link ExtendedRNASecondaryStructure} into bpseq format.
 *
 * <p>The bpseq format is a simple text format where each line contains three columns:
 * <ol>
 *   <li>Index (1‑based position number)</li>
 *   <li>Nucleotide (A, C, G, U, or N)</li>
 *   <li>Pairing partner index (0 if unpaired, otherwise the 1‑based index of the paired nucleotide)</li>
 * </ol>
 *
 * @author Francesco Palozzi
 * @see ExtendedRNASecondaryStructure
 * @see Pair
 */
public class BpseqExporter {

    private static final String[] HEADERS = getHeaders();

    private static String[] getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("id");
        headers.add("nt");
        for (BondType bt : BondType.getLeontisWesthofFamily()) {
            headers.add(bt.getInfo()); // "cWW", "tWW", ..., "tSS"
        }
        return headers.toArray(new String[0]);
    }

    /**
     * Exports an RNA secondary structure to canonical bpseq format.
     *
     * @param structure the RNA secondary structure to export
     * @return a string containing the bpseq canonical representation, with each line
     *         separated by newline characters
     */
    public String printCanonicalBPSEQ(ExtendedRNASecondaryStructure structure) {
        StringBuilder sb = new StringBuilder();

        List<Pair> pairs = structure.getCanonical();
        String seq = this.getSequence(structure);

        for (int i = 0; i < seq.length(); i++) {
            Pair pair = findPair(i, pairs);

            if (pair == null) continue;

            sb.append(i + 1)
                .append(" ")
                .append(seq.charAt(i))
                .append(" ")
                .append((pair.getPos1() == i ? pair.getPos2() : pair.getPos1()) + 1)
                .append("\n");
        }
        return sb.toString();
    }

    /**
     * Exports an RNA secondary structure to an extended bpseq format.
     * The extended format includes, for each position, the pairing partner(s)
     * for every bond type in the Leontis‑Westhof family.
     *
     * <p>The output consists of a header line followed by one line per nucleotide.
     * Each line contains:
     * <ul>
     *   <li>1‑based index</li>
     *   <li>nucleotide character</li>
     *   <li>for each bond type (in the order defined by {@link BondType#getLeontisWesthofFamily()}),
     *       a comma‑separated list of 1‑based partner indices (or 0 if none)</li>
     * </ul>
     *
     * @param structure the RNA secondary structure to export
     * @return a string containing the extended bpseq representation
     */
    public String printExtendedBPSEQ(ExtendedRNASecondaryStructure structure) {
        String seq = this.getSequence(structure);
        Map<BondType, Map<Integer, List<Integer>>> partnerMap = this.buildPartnerMap(structure);
        int[] colWidths = this.getColumnsFormat(structure, seq, partnerMap);

        StringBuilder result = new StringBuilder();

        // Header row (left‑aligned, no trailing space after last column)
        for (int i = 0; i < HEADERS.length; i++) {
            result.append(padRight(HEADERS[i], colWidths[i]));
            if (i < HEADERS.length - 1) result.append(' ');
        }
        result.append('\n');

        for (int i = 0; i < seq.length(); i++) {
            // index (1-based)
            result.append(padRight(String.valueOf(i + 1), colWidths[0]));
            result.append(" ");

            // nucleotide
            String nt = String.valueOf(seq.charAt(i));
            result.append(padRight(nt, colWidths[1]));
            result.append(' ');

            int colIdx = 2;
            for (BondType type : BondType.getLeontisWesthofFamily()) {
                Map<Integer, List<Integer>> posMap = partnerMap.get(type);
                List<Integer> partners = posMap != null ? posMap.get(i) : null;
                String cell = (partners == null || partners.isEmpty())
                    ? "0"
                    : partners
                          .stream()
                          .map(p -> String.valueOf(p + 1))
                          .collect(Collectors.joining(","));

                result.append(padRight(cell, colWidths[colIdx]));
                if (colIdx < colWidths.length - 1) result.append(' ');
                colIdx++;
            }
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * Calculates the minimum required width for each column.
     *
     * @param structure the RNA secondary structure
     * @param seq the RNA sequence
     * @param partnerMap the partner map
     * @return array of 14 integers: column widths (index, nucleotide, 12 LW types)
     */
    private int[] getColumnsFormat(
        ExtendedRNASecondaryStructure structure,
        String seq,
        Map<BondType, Map<Integer, List<Integer>>> partnerMap
    ) {
        int n = seq.length();
        // Initialize with minimum values
        int[] colWidths = Arrays.asList(HEADERS)
            .stream()
            .mapToInt(s -> s.length())
            .toArray();

        // Iterate over all positions
        for (int i = 0; i < n; i++) {
            // Update index column width
            colWidths[0] = Math.max(colWidths[0], String.valueOf(i + 1).length());

            // Update nucleotide column width
            String nt = String.valueOf(seq.charAt(i));
            colWidths[1] = Math.max(colWidths[1], nt.length());

            // Update bond type columns
            int colIdx = 2; // Associated with bond type columns
            for (BondType type : BondType.getLeontisWesthofFamily()) {
                Map<Integer, List<Integer>> posMap = partnerMap.get(type);
                List<Integer> partners = (posMap != null) ? posMap.get(i) : null;
                String cell = "0";
                if (partners != null && !partners.isEmpty()) {
                    cell = partners
                        .stream()
                        .map(p -> String.valueOf(p + 1)) // convert to 1-based
                        .collect(Collectors.joining(","));
                }
                colWidths[colIdx] = Math.max(colWidths[colIdx], cell.length());
                colIdx++;
            }
        }

        return colWidths;
    }

    private Pair findPair(int pos, List<Pair> pairs) {
        return pairs
            .stream()
            .filter(pair -> pair.getPos1() == pos || pair.getPos2() == pos)
            .findFirst()
            .orElse(null);
    }

    private String getSequence(ExtendedRNASecondaryStructure structure) {
        if (structure.getSequence() != null && !structure.getSequence().isEmpty()) {
            return structure.getSequence();
        }

        int maxPos = -1;
        for (Pair pair : structure.getPairs()) {
            maxPos = Math.max(maxPos, Math.max(pair.getPos1(), pair.getPos2()));
        }

        if (maxPos == -1) {
            return "";
        }

        char[] seq = new char[maxPos + 1];
        Arrays.fill(seq, 'N');

        for (Pair pair : structure.getPairs()) {
            String nuc1 = pair.getNucleotide1();
            if (nuc1 != null && !nuc1.isEmpty()) {
                seq[pair.getPos1()] = nuc1.charAt(0);
            }

            String nuc2 = pair.getNucleotide2();
            if (nuc2 != null && !nuc2.isEmpty()) {
                seq[pair.getPos2()] = nuc2.charAt(0);
            }
        }

        return new String(seq);
    }

    private Map<BondType, Map<Integer, List<Integer>>> buildPartnerMap(ExtendedRNASecondaryStructure structure) {
        Map<BondType, Map<Integer, List<Integer>>> map = new HashMap<>();

        for (BondType type : BondType.getLeontisWesthofFamily()) {
            map.put(type, new HashMap<>());
        }

        for (Pair pair : structure.getPairs()) {
            BondType type = pair.getType();
            Map<Integer, List<Integer>> typeMap = map.get(type);
            if (typeMap == null) continue;
            typeMap.computeIfAbsent(pair.getPos1(), pos -> new ArrayList<>()).add(pair.getPos2());
            typeMap.computeIfAbsent(pair.getPos2(), pos -> new ArrayList<>()).add(pair.getPos1());
        }

        return map;
    }

    private String padRight(String s, int width) {
        return String.format("%-" + width + "s", s);
    }
}
