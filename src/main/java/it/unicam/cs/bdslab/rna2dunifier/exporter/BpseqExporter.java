package it.unicam.cs.bdslab.rna2dunifier.exporter;

import it.unicam.cs.bdslab.rna2dunifier.models.BondType;
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

    private static final String HEADER = "Index\tNucleotide\t"
            + String.join("\t", BondType.getLeontisWesthofFamily().stream()
            .map(BondType::getInfo)
            .toList()
    );

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
        String seq = structure.getSequence();

        for (int i = 0; i < seq.length(); i++) {

            Pair pair = findPair(i, pairs);

            if(pair == null) {
                continue;
            }

            sb.append(i+1).append("\t")
                .append(seq.charAt(i)).append("\t")
                .append((pair.getPos1() == i ? pair.getPos2() : pair.getPos1())+1).append("\n");
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
        StringBuilder result = new StringBuilder();
        result.append(HEADER);
        result.append("\n");
        for (int i = 0; i < structure.getSequence().length(); i++) {
            char nucleotide = structure.getSequence().charAt(i);
            result.append(i + 1).append("\t").append(nucleotide).append("\t");
            for (BondType type : BondType.getLeontisWesthofFamily()) {
                int finalI = i;
                List<Integer> matches = structure.getPairs().stream()
                        .filter(pair ->
                                // Filter pairs that match the current bond type and involve the current nucleotide position
                                pair.getType() == type &&
                                        (pair.getPos1() == finalI
                                                || pair.getPos2() == finalI)
                        )
                        .map(p -> (p.getPos1() == finalI ? p.getPos2() : p.getPos1()) + 1)
                        .toList();
                if (matches.isEmpty()) {
                    result.append("0").append("\t");
                } else {
                    result.append(matches.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("0")).append("\t");
                }
            }
            result.append("\n");
        }
        return result.toString();
    }

    private Pair findPair(int pos, List<Pair> pairs) {
        return pairs.stream()
                .filter(pair -> pair.getPos1() == pos || pair.getPos2() == pos)
                .findFirst()
                .orElse(null);
    }
}