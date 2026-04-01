package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
/**
 * Response DTO for Final Weight Test
 */
@Data
public class FinalWeightTestResponse {

    private Long id;
    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;
    private Integer qtyNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;
    private String status;
    private Integer rejected;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private List<SampleResponse> samples;

    @Data
    public static class SampleResponse {
        private Long id;
        private Integer samplingNo;
        private Integer sampleNo;
        private BigDecimal sampleValue;
        private Boolean isRejected;
        private LocalDateTime createdAt;
    }
}

