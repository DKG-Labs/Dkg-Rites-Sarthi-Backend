package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MomentOfResistanceTestResponseDTO {

    private Long id;

    private String batchNumber;
    private String sleeperType;
    private String castingDate;
    private String dateOfTesting;
    private String benchNumber;
    private String sleeperNo;

    private String vendorCode;
    private String plantId;
    private String shift;
    private String testResult;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;

    private List<MomentOfResistanceDetailResponseDTO> details;

    /** Set on create/update: true when this result (a Fail in double testing) rejected the lot */
    private Boolean lotRejected;
    private String rejectedBatchNumber;
    /** Inspection calls (not cancelled/withdrawn) that still carry the rejected lot */
    private List<String> openCallNos;
}
