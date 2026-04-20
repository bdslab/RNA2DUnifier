package it.unicam.cs.bdslab.rna2dunifier.models;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the bond types in RNA structures,
 * including canonical bonds, stacking interactions,
 * and the 12 geometric families of the Leontis-Westhof nomenclature.
 *
 * <p>The 12 Leontis‑Westhof families are encoded as:
 * <ul>
 *   <li>cWW, tWW – Watson‑Crick/Watson‑Crick</li>
 *   <li>cWH, tWH – Watson‑Crick/Hoogsteen</li>
 *   <li>cWS, tWS – Watson‑Crick/Sugar Edge</li>
 *   <li>cHH, tHH – Hoogsteen/Hoogsteen</li>
 *   <li>cHS, tHS – Hoogsteen/Sugar Edge</li>
 *   <li>cSS, tSS – Sugar Edge/Sugar Edge</li>
 * </ul>
 * The prefix 'c' indicates cis orientation, 't' indicates trans.
 */
public enum BondType {

    /** Unknown or unclassified bond type. */
    UNKNOWN("unknown"),

    /** Stacking interaction */
    STACKING("stacking"),

    /** 1. Cis Watson–Crick/Watson–Crick Antiparallel */
    LEONTIS_WESTHOF_cWW("cWW"),

    /** 2. Trans Watson–Crick/Watson–Crick Parallel */
    LEONTIS_WESTHOF_tWW("tWW"),

    /** 3. Cis Watson–Crick/Hoogsteen Parallel */
    LEONTIS_WESTHOF_cWH("cWH"),

    /** 4. Trans Watson–Crick/Hoogsteen Antiparallel */
    LEONTIS_WESTHOF_tWH("tWH"),

    /** 5. Cis Watson–Crick/Sugar Edge Antiparallel */
    LEONTIS_WESTHOF_cWS("cWS"),

    /** 6. Trans Watson–Crick/Sugar Edge Parallel */
    LEONTIS_WESTHOF_tWS("tWS"),

    /** 7. Cis Hoogsteen/Hoogsteen Antiparallel */
    LEONTIS_WESTHOF_cHH("cHH"),

    /** 8. Trans Hoogsteen/Hoogsteen Parallel */
    LEONTIS_WESTHOF_tHH("tHH"),

    /** 9. Cis Hoogsteen/Sugar Edge Parallel */
    LEONTIS_WESTHOF_cHS("cHS"),

    /** 10. Trans Hoogsteen/Sugar Edge Antiparallel */
    LEONTIS_WESTHOF_tHS("tHS"),

    /** 11. Cis Sugar Edge/Sugar Edge Antiparallel */
    LEONTIS_WESTHOF_cSS("cSS"),

    /** 12. Trans Sugar Edge/Sugar Edge Parallel */
    LEONTIS_WESTHOF_tSS("tSS");

    private final String info;

    BondType(String info) {
        this.info = info;
    }

    /**
     * Returns the string representation of the bond type.
     *
     * @return the identifying string (e.g., "cWW", "tWH", "stacking", "unknown")
     */
    public String getInfo() {
        return info;
    }

    /**
     * Retrieves the BondType instance from its textual representation.
     *
     * @param text the string to search for (e.g., "cWW"); case‑insensitive,
     *             can be {@code null}
     * @return the corresponding BondType, or {@link #UNKNOWN} if not found or if text is null
     */
    public static BondType fromString(String text) {
        if (text == null) {
            return UNKNOWN;
        }
        for (BondType b : BondType.values()) {
            if (b.info.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return UNKNOWN;
    }

    /**
     * Checks whether this bond type has a cis orientation.
     * <p>
     * Cis orientation means the two glycosidic bonds are on the same side of the base‑pair plane.
     *
     * @return {@code true} if this is a cis Leontis‑Westhof type (cWW, cWH, cWS, cHH, cHS, cSS);
     *         {@code false} otherwise (including UNKNOWN and STACKING)
     */
    public boolean isCis() {
        return this == LEONTIS_WESTHOF_cWW || this == LEONTIS_WESTHOF_cWH || this == LEONTIS_WESTHOF_cWS ||
               this == LEONTIS_WESTHOF_cHH || this == LEONTIS_WESTHOF_cHS || this == LEONTIS_WESTHOF_cSS;
    }

    /**
     * Checks whether this bond type has a trans orientation.
     * <p>
     * Trans orientation means the two glycosidic bonds are on opposite sides of the base‑pair plane.
     *
     * @return {@code true} if this is a trans Leontis‑Westhof type (tWW, tWH, tWS, tHH, tHS, tSS);
     *         {@code false} otherwise (including UNKNOWN and STACKING)
     */
    public boolean isTrans() {
        return this == LEONTIS_WESTHOF_tWW || this == LEONTIS_WESTHOF_tWH || this == LEONTIS_WESTHOF_tWS ||
               this == LEONTIS_WESTHOF_tHH || this == LEONTIS_WESTHOF_tHS || this == LEONTIS_WESTHOF_tSS;
    }

    /**
     * Checks whether this bond type represents a canonical (Watson‑Crick/Watson‑Crick) base pair.
     *
     * @return {@code true} for cWW or tWW; {@code false} otherwise (including UNKNOWN and STACKING)
     */
    public boolean isCanonical() {
        return this == LEONTIS_WESTHOF_cWW || this == LEONTIS_WESTHOF_tWW;
    }

    /**
     * Returns a list of the 12 Leontis-Westhof bond types in a consistent order.
     * @return List of BondType representing the Leontis-Westhof geometric families, excluding UNKNOWN, CANONICAL, NON_CANONICAL, and STACKING.
     */
    public static List<BondType> getLeontisWesthofFamily() {
        return Arrays.stream(values())
                .filter(bt -> bt != BondType.UNKNOWN
                        && bt != BondType.STACKING
                )
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .toList();
    }
}