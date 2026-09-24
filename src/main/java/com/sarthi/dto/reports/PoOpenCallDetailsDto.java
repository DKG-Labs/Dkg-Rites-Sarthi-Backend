package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoOpenCallDetailsDto {
    private String callNo;
    private String callDate;
    private String desiredDate;
    private Long offeredQty;
    private String stage;
    private String status;
    private String placeOfInspection;
}
