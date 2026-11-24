package com.sarthi.Sms.dto.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeldDimCheckDto {
    private String jointNumber;
    private String LH;
    private String oneMeterRH;
    private String oneMeterVertical;
    private String tenCmLH;
    private String tenCmRH;
    private String tenCmVertical;
    private String visual;
    private String marking;
    private String remark;

}
