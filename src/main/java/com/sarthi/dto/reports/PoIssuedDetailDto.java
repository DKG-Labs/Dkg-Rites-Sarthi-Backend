package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoIssuedDetailDto {
    private String railwayZone;
    private String poNumber;
    private LocalDateTime poDate;
    private String vendor;
    private Long poQuantity;
    private String uom;
    private Long acceptedQtyAfterFinalInspection;
    private Long balanceQuantity;
}
