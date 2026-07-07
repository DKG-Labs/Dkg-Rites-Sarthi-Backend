package com.sarthi.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating IC Numbers
 * New Format: E[TYPE]-[MMDDYY][NNNN]
 * Where:
 * - E = Fixed prefix representing "ERC"
 * - [TYPE] = Single letter (R=Raw Material, P=Process, F=Final)
 * - [MMDDYY] = Current month, date, and 2-digit year (zero-padded)
 * - [NNNN] = Sequential serial number (4 digits, zero-padded, resets daily)
 *
 * Examples: ER-0106260001, EP-0106260001, EF-0106260001
 */
@Component
public class IcNumberGenerator {

    private static final String ERC_PREFIX = "E";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMddyy");

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

        // Format: E + TYPE + - + MMDDYY + NNNN
        return String.format("%s%s-%s%04d", ERC_PREFIX, typeCode, dateStr, sequence);
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
        if (icNumber == null || (icNumber.length() != 11 && icNumber.length() != 13)) {
            throw new IllegalArgumentException("Invalid IC number format. Expected format: E[TYPE]-[MMDD][NNNN] or E[TYPE]-[MMDDYY][NNNN]");
        }

        if (!icNumber.startsWith(ERC_PREFIX)) {
            throw new IllegalArgumentException("IC number must start with 'E'");
        }

        if (icNumber.charAt(2) != '-') {
            throw new IllegalArgumentException("IC number must have hyphen at position 3");
        }

        String typeCode = icNumber.substring(1, 2);
        String month = icNumber.substring(3, 5);
        String day = icNumber.substring(5, 7);
        String sequence = (icNumber.length() == 13) ? icNumber.substring(9, 13) : icNumber.substring(7, 11);

        return new String[]{typeCode, month, day, sequence};
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

