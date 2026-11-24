package com.sarthi.Sms.dto.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WeldJointTestDto {
    private String jointNumber;
    private String weldOperator;
    private String usfdOperator;
    private String result;
    private String remark;
    private String panelRemark;
}
