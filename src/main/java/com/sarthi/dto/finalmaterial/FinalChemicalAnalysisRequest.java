package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for Final Inspection - Chemical Analysis
 * 
 * Frontend sends data like:
 * {
 *   "inspectionCallNo": "EP-01090004",
 *   "lotNo": "lot2",
 *   "heatNo": "T844929",
 *   "carbonPercent": 0.55,
 *   "siliconPercent": 1.75,
 *   "manganesePercent": 0.90,
 *   "sulphurPercent": 0.015,
 *   "phosphorusPercent": 0.020,
 *   "remarks": "Sample passed all tests",
 *   "createdBy": "user123"
 * }
 */
@Data
public class FinalChemicalAnalysisRequest {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;
    private Integer sampleNo;
    private BigDecimal carbonPercent;
    private BigDecimal siliconPercent;
    private BigDecimal manganesePercent;
    private BigDecimal sulphurPercent;
    private BigDecimal phosphorusPercent;
    private String remarks;
    private String createdBy;
    private String updatedBy;
}

