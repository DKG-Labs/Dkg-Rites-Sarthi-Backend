package com.sarthi.Sms.dto.rolling;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class FinishingVerificationReqDto {
    private String dutyId;

    private List<HotStampingDto> hotStampingList;
    private List<PreCamberingDto> preCamberingList;

    private String straightnessRemarks;
    private String remarks;

    private String smR1Hrsm;
    private String smR4Hrsm;
    private String smR6Hrsm;
    private String smR8Hrsm;
    private String smR1Vrsm;
    private String smR4Vrsm;
    private String smR6Vrsm;
    private String smR8Vrsm;
    private Boolean smIsSatisfactory;

    private String smSrR1Hrsm;
    private String smSrR2Hrsm;
    private String smSrR3Hrsm;
    private String smSrR4Hrsm;
    private String smSrR1Vrsm;
    private String smSrR2Vrsm;
    private String smSrR3Vrsm;
    private String smSrR4Vrsm;
    private Boolean smSrIsSatisfactory;

    private String smLrR1Hrsm;
    private String smLrR2Hrsm;
    private String smLrR3Hrsm;
    private String smLrR4Hrsm;
    private String smLrR1Vrsm;
    private String smLrR2Vrsm;
    private String smLrR3Vrsm;
    private String smLrR4Vrsm;
    private Boolean smLrIsSatisfactory;

}
