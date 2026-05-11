package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.util.List;

@Data
public class PlantSetupRequestDto {
    private String vendorName;
    private String vendorCode;
    private Integer numberOfUnits;
    private String plantId;
    private String shift;
    private Long createdBy;
    private Long updatedBy;
    private List<PlantUnitRequestDto> units;
}
