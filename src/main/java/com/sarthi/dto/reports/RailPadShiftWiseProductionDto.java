package com.sarthi.dto.reports;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadShiftWiseProductionDto {
    private LocalDate date;
    private String shift;
    private String poNo;
    private long noOfBatches;
    private long producedQty;
    private long acceptedQty;
    private long rejectedQty;
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String plantName;
}
