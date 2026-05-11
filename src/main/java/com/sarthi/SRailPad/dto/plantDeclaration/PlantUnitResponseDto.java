package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.util.List;

@Data
public class PlantUnitResponseDto {
    private Long id;
    private String unitName;
    private String address;
    private Integer numLines;
    private List<UnitProductResponseDto> products;
}
