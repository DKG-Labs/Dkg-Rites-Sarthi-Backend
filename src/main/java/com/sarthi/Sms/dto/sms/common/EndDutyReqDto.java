package com.sarthi.Sms.dto.sms.common;

import lombok.Data;

@Data
public class EndDutyReqDto {
    private String dutyId;
    private String shiftRemarks;
    private Boolean ieConfirmation;
    private String machineSNoHead;
    private String machineSNoFoot;
    private String probeDetailsHead;
    private String probeDetailsFoot;
    private String probeDbHead;
    private String probeDbFoot;
    private String remarksHead;
    private String remarksFoot;

    private Integer jointsWeldedPrevShiftBsp2;
    private Integer jointsWeldedPrevShiftBsp1;
    private Integer jointsWeldedPrevShiftBsp4;
}
