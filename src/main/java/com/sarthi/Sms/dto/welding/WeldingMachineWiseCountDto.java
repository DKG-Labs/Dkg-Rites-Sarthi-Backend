package com.sarthi.Sms.dto.welding;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeldingMachineWiseCountDto {
    private String machineNo;
    private Long tltCount;
    private Long hardnessCount;
    private Long macroCount;
    private Long microCount;
}
