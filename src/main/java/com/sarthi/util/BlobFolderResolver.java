package com.sarthi.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility to resolve Azure Blob Storage folder hierarchies for ERC, Railpad, and Sleeper modules.
 * Structure:
 * - ERC:
 *     - Raw Material (ER prefix)   -> erc/ER
 *     - Process (EP prefix)        -> erc/EP
 *     - Final Product (EF prefix)  -> erc/EF
 * - Railpad:
 *     - Process (RPP prefix)       -> railpad/RPP
 *     - Final Product (RPF prefix) -> railpad/RPF
 * - Sleeper:
 *     - Sleeper (SF / SL / Sleeper)-> sleeper
 */
@Slf4j
public final class BlobFolderResolver {

    private BlobFolderResolver() {}

    public static String resolveFolder(String callNo, String moduleType) {
        String c = (callNo != null ? callNo.trim().toUpperCase() : "");
        String m = (moduleType != null ? moduleType.trim().toUpperCase() : "");

        // 1. ERC types
        if (c.startsWith("ER-") || c.startsWith("ER_") || c.contains("/ER-") || c.contains("_ER-") ||
            (m.contains("ERC") && (c.startsWith("ER") || m.contains("RAW") || m.contains("RM")))) {
            return "erc/ER";
        }
        if (c.startsWith("EP-") || c.startsWith("EP_") || c.contains("/EP-") || c.contains("_EP-") ||
            (m.contains("ERC") && (c.startsWith("EP") || m.contains("PROCESS")))) {
            return "erc/EP";
        }
        if (c.startsWith("EF-") || c.startsWith("EF_") || c.contains("/EF-") || c.contains("_EF-") ||
            (m.contains("ERC") && (c.startsWith("EF") || m.contains("FINAL")))) {
            return "erc/EF";
        }

        // 2. Railpad types
        if (c.startsWith("RPP") || (m.contains("RAIL") && (c.contains("RPP") || m.contains("PROCESS")))) {
            return "railpad/RPP";
        }
        if (c.startsWith("RPF") || (m.contains("RAIL") && (c.contains("RPF") || m.contains("FINAL")))) {
            return "railpad/RPF";
        }

        // 3. Sleeper types
        if (c.startsWith("SF") || c.startsWith("SL") || m.contains("SLEEPER")) {
            return "sleeper";
        }

        // Module fallbacks
        if (m.contains("ERC")) {
            return "erc/EF";
        }
        if (m.contains("RAIL")) {
            return "railpad/RPF";
        }
        return "sleeper";
    }
}
