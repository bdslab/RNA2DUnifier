package it.unicam.cs.bdslab.rna2dunifier.models;

import java.util.Optional;

/**
 * Represents the bond types in RNA structures,
 * including canonical bonds, stacking interactions,
 * and the 12 geometric families of the Leontis-Westhof nomenclature.
 */
public enum BondType {

    /** Unknown or unclassified bond type. */
    UNKNOWN("unknown"),

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
     * @return The identifying string (e.g., "cWW", "tWH").
     */
    public String getInfo() {
        return info;
    }

    /**
     * Retrieves the BondType instance from its textual representation.
     *
     * @param text The string to search for (e.g., "cWW").
     * @return The corresponding BondType, or UNKNOWN if not found or if the text is null.
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

    public boolean isCis() {
        return this == LEONTIS_WESTHOF_cWW || this == LEONTIS_WESTHOF_cWH || this == LEONTIS_WESTHOF_cWS ||
               this == LEONTIS_WESTHOF_cHH || this == LEONTIS_WESTHOF_cHS || this == LEONTIS_WESTHOF_cSS;
    }

    public boolean isTrans() {
        return this == LEONTIS_WESTHOF_tWW || this == LEONTIS_WESTHOF_tWH || this == LEONTIS_WESTHOF_tWS ||
               this == LEONTIS_WESTHOF_tHH || this == LEONTIS_WESTHOF_tHS || this == LEONTIS_WESTHOF_tSS;
    }

    public boolean isCanonical() {
        return this == LEONTIS_WESTHOF_cWW || this == LEONTIS_WESTHOF_tWW;
    }
}