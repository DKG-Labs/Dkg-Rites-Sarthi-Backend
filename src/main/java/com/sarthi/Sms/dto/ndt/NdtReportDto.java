package com.sarthi.Sms.dto.ndt;

import java.sql.Date;

import lombok.Data;

@Data
public class NdtReportDto {
    private Date date;
    private String ndt;
    private String railGrade;
    private String railSection;
    private Integer totalRailsInspected;
    private Integer totalUtMarkedRails;
    private Integer totalUtSuspectedRails;
    private Integer totalRecycledRails;
    private Integer totalUtRejectedRails;
    private Integer ectSuspectedRails;
    private Integer bodyStraightnessResultA;
    private Integer bodyStraightnessResultA1;
    private Integer bodyStraightnessResultMarked;
    private Integer totalProfileMarked;
    private Integer railLength;

}
