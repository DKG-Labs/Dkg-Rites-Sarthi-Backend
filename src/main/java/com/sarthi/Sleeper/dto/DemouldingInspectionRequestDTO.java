package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

@Data
public class DemouldingInspectionRequestDTO {


    private String lineShedNo;

    private String inspectionDate;   // yyyy-MM-dd

    private String inspectionTime;   // HH:mm

    private String castingDate;      // yyyy-MM-dd

    private String batchNo;

    private String benchNo;

    private String sleeperType;

    private String processStatus;

    private String visualCheck;

    private String dimCheck;         // ALL_OK / PARTIAL / REJECTED

    private String overallRemarks;

    private String createdBy;

    private String updatedBy;

    private List<DemouldingDefectiveSleeperDTO> defectiveSleepers;
}
