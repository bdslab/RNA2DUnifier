package it.unicam.cs.bdslab.rna2dunifier.exporter;

import it.unicam.cs.bdslab.rna2dunifier.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.rna2dunifier.models.Pair;

import java.util.List;

/**
 *
 */
public class BpseqExporter {

    /**
     *
     * @param structure
     * @return
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

    private static int findPairIndex(int pos, List<Pair> pairs) {
        for (Pair p : pairs) {
            if (p.getPos1() == pos) return p.getPos2();
            if (p.getPos2() == pos) return p.getPos1();
        }
        return 0;
    }
}
