package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearestPlantDto {
    private String type;          // ERC, SLEEPER, RAIL_PAD
    private String plantId;
    private String plantName;
    private String companyName;
    private String address;
    private Double plantLatitude;
    private Double plantLongitude;
    private Double distanceMeters;
}
