package com.sarthi.Sms.dto.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortRailRejectionAnalysisDto {
    private String defect26m;
    private Integer defect26mCount;
    private String defect13m;
    private Integer defect13mCount;
}