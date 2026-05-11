package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlantSetupResponseDto {
    private Long id;
    private String vendorName;
    private String vendorCode;
    private Integer numberOfUnits;
    private String plantId;
    private String shift;
    private String status;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
    private List<PlantUnitResponseDto> units;
}
