package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IcAnnexuresReportDto {
    private String vendorName;
    private String railwayShortName;
    private String poNumberOnly;
    private String poSerialNumber;
    private String callNumber;
    private String icNumber;
    private String stage;
    private String icIssuedDate;
    private String itemCatDescr;
}
