package com.sarthi.dto.IcDtos;

import lombok.Data;
import java.util.List;

/**
 * DTO for Final Inspection Details Request
 * Contains summary information for Final inspection
 */
@Data
public class FinalInspectionDetailsRequestDto {

    // ---- RM IC REFERENCE (single - kept for backward compat) ----
    private String rmIcNumber;

    // ---- RM IC REFERENCES (multiple - preferred for new calls) ----
    private List<String> rmIcNumbers;

    // ---- PROCESS IC REFERENCE (single - kept for backward compat) ----
    private String processIcNumber;

    // ---- PROCESS IC REFERENCES (multiple - preferred for new calls) ----
    private List<String> processIcNumbers;

    // ---- PLACE OF INSPECTION ----
    private Integer companyId;
    private String companyName;
    private Integer unitId;
    private String unitName;
    private String unitAddress;

    // ---- SUMMARY INFORMATION ----
    private Integer totalLots;
    private Integer totalOfferedQty;
}
