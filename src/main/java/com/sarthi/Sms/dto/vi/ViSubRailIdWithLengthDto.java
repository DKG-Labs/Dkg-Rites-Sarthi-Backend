package com.sarthi.Sms.dto.vi;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViSubRailIdWithLengthDto {
    private String railId;
    private String subRailId;
    private BigDecimal acceptedLength;
}
