package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RailFinalWeightTestResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    private Integer n1;
    private Integer ac1;
    private Integer re1;
    private Integer n2;
    private Integer ac2;
    private Integer re2;
    private Double minWeight;
    private Double maxWeight;
    private Boolean isSecondActive;

    private String weightStatus;
    private Integer notOk1;
    private Integer notOk2;
    private Integer totalNotOk;
    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;

    private List<SampleDto> samples;

    @Data
    public static class SampleDto {
        private Long id;
        private Integer samplingNo;
        private Integer sampleNo;
        private Double sampleValue;
        private Boolean isRejected;
    }
}
