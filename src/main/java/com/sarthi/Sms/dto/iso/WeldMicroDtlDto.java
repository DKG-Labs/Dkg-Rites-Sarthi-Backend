package com.sarthi.Sms.dto.iso;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeldMicroDtlDto {
    private String date;
    private String railSection;
    private Boolean isMicroFree;
    private Boolean isAstmCoarser;
    private String remark;
}
