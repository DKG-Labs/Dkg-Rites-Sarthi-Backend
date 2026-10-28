package com.sarthi.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating IC Numbers
 * Format: E[TYPE]-[YYMMDD][NN]
 * Where:
 * - E = Fixed prefix representing "ERC"
 * - [TYPE] = Single letter (R=Raw Material, P=Process, F=Final)
 * - [YYMMDD] = 2-digit year, month, day (zero-padded)
 * - [NN] = Sequential serial number (2 digits, zero-padded, resets daily)
 *
 * Examples: ER-26102801, EP-26102801, EF-26102801
 */
@Component
public class IcNumberGenerator {

    private static final String ERC_PREFIX = "E";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    /**
     * Generate IC Number for a given type and sequence
     * @param typeOfCall - Type of inspection call (Raw Material, Process, Final)
     * @param sequence - Sequence number (should reset daily)
     * @return Generated IC number in format E[TYPE]-[MMDD][NNNN]
     */
    public String generateIcNumber(String typeOfCall, long sequence) {
        String typeCode = getTypeCodeForCall(typeOfCall);
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);

        // Format: E + TYPE + - + YYMMDD + NN
        return String.format("%s%s-%s%02d", ERC_PREFIX, typeCode, dateStr, sequence);
    }

    /**
     * Get single-letter type code for inspection call type
     * @param typeOfCall - Type of inspection call
     * @return Single letter code (R, P, or F)
     */
    private String getTypeCodeForCall(String typeOfCall) {
        switch (typeOfCall) {
            case "Raw Material":
                return "R";
            case "Process":
                return "P";
            case "Final":
                return "F";
            default:
                throw new IllegalArgumentException("Invalid type of call: " + typeOfCall);
        }
    }

    /**
     * Parse IC Number to extract components
     * @param icNumber - IC number to parse
     * @return Array with [typeCode, month, day, sequence]
     */
    public String[] parseIcNumber(String icNumber) {
        if (icNumber == null || icNumber.length() != 11) {
            throw new IllegalArgumentException("Invalid IC number format. Expected format: E[TYPE]-[YYMMDD][NN]");
        }

        if (!icNumber.startsWith(ERC_PREFIX)) {
            throw new IllegalArgumentException("IC number must start with 'E'");
        }

        if (icNumber.charAt(2) != '-') {
            throw new IllegalArgumentException("IC number must have hyphen at position 3");
        }

        // Format: E[TYPE]-[YY][MM][DD][NN]  e.g. ER-26102801
        String typeCode = icNumber.substring(1, 2);
        String yy = icNumber.substring(3, 5);
        String mm = icNumber.substring(5, 7);
        String dd = icNumber.substring(7, 9);
        String sequence = icNumber.substring(9, 11);

        return new String[]{typeCode, yy, mm, dd, sequence};
    }

    /**
     * Get type of call from IC number
     * @param icNumber - IC number
     * @return Type of call (Raw Material, Process, or Final)
     */
    public String getTypeFromIcNumber(String icNumber) {
        String[] parts = parseIcNumber(icNumber);
        String typeCode = parts[0];

        switch (typeCode) {
            case "R":
                return "Raw Material";
            case "P":
                return "Process";
            case "F":
                return "Final";
            default:
                throw new IllegalArgumentException("Unknown type code: " + typeCode);
        }
    }
}

