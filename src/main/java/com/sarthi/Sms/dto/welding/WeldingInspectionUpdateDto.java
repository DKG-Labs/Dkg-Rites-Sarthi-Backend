package com.sarthi.Sms.dto.welding;

import lombok.Data;

import java.util.List;

@Data

public class WeldingInspectionUpdateDto {

    private String panelId; // Panel ID that needs to be updated
    private Integer numberOfJoints; // Number of joints in the panel
    private Integer panelLength; // Panel length
    private String panelDecision; // Panel decision (e.g., Approved, Hold, etc.)
    private String panelRemark; // Panel remark
    private Boolean frontResult; // Front result of the inspection
    private Boolean backResult; // Back result of the inspection
    private String frontResultReasonForNotOk; // Reason for front result failure (if applicable)
    private String backResultReasonForNotOk; // Reason for back result failure (if applicable)
    private String frontRemark; // Front remark
    private String backRemark; // Back remark
    private List<WeldingDetailDto> weldingInspectionDetails; // List of welding inspection details
}
