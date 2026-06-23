package com.sarthi.Sms.dto.sms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BloomInspReqDto {
    private String dutyId;
    private String castNo;
    private Integer noOfPrimeBlooms;
    private Integer noOfCoBlooms;
    private String bloomIdentification;
    private BigDecimal lengthOfBlooms;
    private String surfaceConditionOfBlooms;
    private Integer noOfPrimeBloomsRejected;
    private Integer noOfCoBloomsRejected;
    private String remark;
}
