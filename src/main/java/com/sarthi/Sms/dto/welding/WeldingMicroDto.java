package com.sarthi.Sms.dto.welding;

import lombok.Data;

@Data
public class WeldingMicroDto {
    private String testType;
    private String jointNo;
    private Boolean isMicroFree;
    private Boolean isAstmCoarser;
    private String remarks;
    private String dutyId;
    private String machineNo;
}
