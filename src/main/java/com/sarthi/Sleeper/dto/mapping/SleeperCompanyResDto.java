package com.sarthi.Sleeper.dto.mapping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SleeperCompanyResDto {

    private String companyName;

    private String vendorCode;

    private String poiCode;
}