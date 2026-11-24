package com.sarthi.Sms.dto.welding;

import lombok.Data;
import java.util.List;

@Data
public class WeldingInspectionDto {
    private Integer id;
    private String panelId;
    private Integer noOfJoints;
    private Integer panelLength;
    private String panelDecision;
    private String panelRemark;
    private String frontResult;
    private String backResult;
    private String frontResultReasonForNotOk;
    private String backResultReasonForNotOk;
    private String frontRemark;
    private String backRemark;
    private String dutyId;

    // List of WeldingInspectionDetailDto for welding_inspection_detail table
    private List<WeldingDetailDto> weldList;

}

