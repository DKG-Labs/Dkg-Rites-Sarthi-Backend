package com.sarthi.SRailPad.dto;

import lombok.Data;
import java.util.List;

@Data
public class RailVendorResponseDTO {
    private String vendorCode;
    private String companyName;
    private List<RailPlantDTO> plants;
}
