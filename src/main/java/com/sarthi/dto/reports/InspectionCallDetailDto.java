package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionCallDetailDto {
    private String inspectionCallNumber;
    private String vendor;
    private String callSubmissionDateTime;
    private String stageOfInspection;
    private String poSrNo;
    private String dpDate;
    private String status;

    private String mainStatus;
    private String subStatus;
}
