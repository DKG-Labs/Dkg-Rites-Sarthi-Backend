package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

@Data
public class VendorResponseDTO {

    private String vendorCode;
    private String companyName;
    private List<PlantDTO> plants;
}
