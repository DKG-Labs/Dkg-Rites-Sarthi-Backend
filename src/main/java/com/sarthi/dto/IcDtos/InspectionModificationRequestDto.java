package com.sarthi.dto.IcDtos;

import lombok.Data;

@Data
public class InspectionModificationRequestDto {

    private InspectionCallRequestDto inspectionCall;

    private RmInspectionDetailsRequestDto rmInspectionDetails;
}
