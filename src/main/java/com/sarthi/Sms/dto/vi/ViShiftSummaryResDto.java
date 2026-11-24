package com.sarthi.Sms.dto.vi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ViShiftSummaryResDto {
    private String heatNo;
    private String railId;
    private String dutyId;
    private LocalDate date;
    private String shift;
    private String serialNo;
    private String heatStatus;
    private BigDecimal actualOfferedLength;
    private String utFeedback;
    private String dimFeedback;
    private String visualFeedback;
    private Boolean gaugingEndStraightness;
    private Integer rej13;
    private Integer rej12;
    private Integer rej11;
    private Integer rej10;
    private BigDecimal rejCompLength;
    private String remarks;
    private String lineNumber; // From duty entity
    private String railGrade;   // From duty entity
    private String railSection; // From duty entity
    private String mill;        // From duty entity
    private BigDecimal standardOfferedLength; // From duty entity

    // Related data
    private List<ViAcceptDtlsDto> acptDataList;
    private List<ViDefectDtlsDto> defectDataList;
}