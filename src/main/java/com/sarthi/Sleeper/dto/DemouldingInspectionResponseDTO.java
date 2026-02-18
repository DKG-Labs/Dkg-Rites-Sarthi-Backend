package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DemouldingInspectionResponseDTO {

    private Long id;

    private String lineShedNo;

    private String inspectionDate;

    private String inspectionTime;

    private String castingDate;

    private String batchNo;

    private String benchNo;

    private String sleeperType;

    private String processStatus;

    private String visualCheck;

    private String dimCheck;

    private String overallRemarks;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String status;

    private List<DemouldingDefectiveSleeperDTO> defectiveSleepers;
}
