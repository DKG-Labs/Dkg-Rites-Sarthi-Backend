package com.sarthi.udmcomplaint.dto;

import lombok.Data;

import java.util.List;

@Data
public class UdmComplaintStageDto {
    private Long id;
    private String stage;
    private Integer roundNo;
    private String path;
    private Boolean flagFailed90;
    private Boolean flagQtyOver5;
    private Boolean flagCritical;
    private String stageDate;
    private String jiTeam;
    private String jiOutcome;
    private String auditType;
    private String findings;
    private String attributableTo;
    private String rootCause;
    private List<String> correctiveActions;
    private List<String> preventiveActions;
    private String vendorCompliance;
    private String capaImplemented;
    private String remarks;
    private Integer createdBy;
    private String createdAt;
}
