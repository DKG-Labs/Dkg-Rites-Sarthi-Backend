package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UnitProductRequestDto {
    private String productName;
    private String approvalNo;
    private LocalDate approvalDate;
    private Integer capacity;
}
