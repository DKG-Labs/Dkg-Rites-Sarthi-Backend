package com.sarthi.Sms.dto.rolling;

import lombok.Data;

@Data
public class TestSampleAcptDto {
    private String dutyId;
    private String railGrade;
    private String sampleType;
    private String sampleId;
    private String heatNo;
    private Integer strand;
    private String sampleLot;
    private Boolean chemical = false;
    private Boolean n2 = false;
    private Boolean fwtSt = false;
    private Boolean mechanical = false;
    private Boolean sp = false;
    private Boolean ir = false;
    private Boolean o2 = false;
    private Boolean fwtHs = false;
    private Boolean fwtStSr = false;
    private Boolean tensileFoot = false;
    private Boolean micro = false;
    private Boolean decarb = false;
    private Boolean rsh = false;
    private Boolean ph = false;
    private Boolean tensile = false;
}
