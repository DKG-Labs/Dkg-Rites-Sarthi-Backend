package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.util.List;

@Data
public class PlantUnitRequestDto {
    private String unitName;
    private String address;
    private Integer numLines;
    private List<UnitProductRequestDto> products;
}
