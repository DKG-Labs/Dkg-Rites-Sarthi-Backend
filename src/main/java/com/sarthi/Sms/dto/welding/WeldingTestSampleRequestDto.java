package com.sarthi.Sms.dto.welding;

import lombok.Data;

@Data
public class WeldingTestSampleRequestDto {

    private String testType;
    private String jointNo;
    private String railGrade;
    private String railSection;
    private String plant;
    private Boolean status;
    private String dutyId;
}
