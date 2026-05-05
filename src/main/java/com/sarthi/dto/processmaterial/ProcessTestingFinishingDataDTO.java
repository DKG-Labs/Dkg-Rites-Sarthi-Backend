package com.sarthi.dto.processmaterial;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProcessTestingFinishingDataDTO {

    private Long id;
    private String inspectionCallNo;
    private String poNo;
    private String lineNo;
    private String shift;
    private Integer hourIndex;
    private String hourLabel;
    private Boolean noProduction;
    private String lotNo;
    private BigDecimal toeLoad1;
    private BigDecimal toeLoad2;
    private BigDecimal weight1;
    private BigDecimal weight2;
    private String paintIdentification1;
    private String paintIdentification2;
    private String ercCoating1;
    private String ercCoating2;
    private Integer acceptedQty;
    private Integer rejectedQty;

    // Separate rejection fields for each measurement
    private Integer toeLoadRejected;
    private Integer weightRejected;
    private Integer paintIdentificationRejected;
    private Integer ercCoatingRejected;

    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
