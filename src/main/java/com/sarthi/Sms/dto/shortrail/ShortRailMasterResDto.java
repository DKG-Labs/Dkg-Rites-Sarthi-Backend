package com.sarthi.Sms.dto.shortrail;

import java.util.List;

import lombok.Data;

@Data
public class ShortRailMasterResDto {
    private String dutyId;
    private String railSectionInspected;
    private String railGradeInspected;
    private Integer acceptance26mClA;
    private Integer acceptance26mClA01;
    private Integer acceptance26mClB;
    private Integer acceptance26mIu;
    private Integer acceptance25mClA;
    private Integer acceptance25mClA01;
    private Integer acceptance25mClB;
    private Integer acceptance25mIu;
    private Integer acceptance24mClA;
    private Integer acceptance24mClA01;
    private Integer acceptance24mClB;
    private Integer acceptance24mIu;
    private Integer acceptance13mClA;
    private Integer acceptance13mClA01;
    private Integer acceptance13mClB;
    private Integer acceptance13mIu;
    private Integer acceptance12mClA;
    private Integer acceptance12mClA01;
    private Integer acceptance12mClB;
    private Integer acceptance12mIu;
    private Integer acceptance11mClA;
    private Integer acceptance11mClA01;
    private Integer acceptance11mClB;
    private Integer acceptance11mIu;
    private Integer acceptance10mClA;
    private Integer acceptance10mClA01;
    private Integer acceptance10mClB;
    private Integer acceptance10mIu;
    private Integer rejection26m;
    private Integer rejection13m;
    private Integer cutbart26m;
    private Integer cutbart13m;
    private Integer refinish26m;
    private Integer refinish13m;
    private Integer utnpRejection26m;
    private Integer utnpRejection13m;
    private Integer utnpCutbart26m;
    private Integer utnpCutbart13m;
    private Integer utnpRefinish26m;
    private Integer utnpRefinish13m;
    private String remarks;
    private List<RejectionAnalysisDtlsResDto> rejectionAnalysisDtls;
}

