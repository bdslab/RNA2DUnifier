package it.unicam.cs.bdslab.rna2dunifier.exporter;

import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;

import java.util.List;

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

    /**
     * Exports an RNA secondary structure to bpseq format.
     *
     * @param structure the RNA secondary structure to export
     * @return a string containing the bpseq representation, with each line
     *         separated by newline characters
     */
    public static String export(ExtendedRNASecondaryStructure structure) {
        StringBuilder sb = new StringBuilder();

        List<Pair> pairs = structure.getPairs();
        String seq = structure.getSequence();

        for (int i = 0; i < seq.length(); i++) {
            int pairIndex = findPairIndex(i, pairs);
            sb.append(i + 1).append("\t")
                    .append(seq.charAt(i)).append("\t")
                    .append(pairIndex).append("\n");
        }
        return sb.toString();
    }

    /**
     * Finds the pairing partner for a given position in the sequence.
     *
     * @param pos   the zero‑based index of the nucleotide to look up
     * @param pairs the list of all pairs in the structure
     * @return the 1‑based index of the paired nucleotide if found,
     *         or 0 if the nucleotide is unpaired
     */
    private static int findPairIndex(int pos, List<Pair> pairs) {
        for (Pair p : pairs) {
            if (p.getPos1() == pos) return p.getPos2() + 1; // convert to 1‑based
            if (p.getPos2() == pos) return p.getPos1() + 1; // convert to 1‑based
        }
        return 0;
    }
}