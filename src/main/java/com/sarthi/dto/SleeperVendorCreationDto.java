package com.sarthi.dto;

import lombok.Data;
import java.util.List;

@Data
public class SleeperVendorCreationDto {
    private Integer userId;
    private String companyName;
    private String vendorCode;
    private String email;
    private String mobileNumber;
    private String password;
    private String status = "Active";
    private String createdBy;

    // Single Unit Info (mapped to sleeper_pincode_poi_mapping)
    private Long unitId;
    private String unitName;
    private String unitPinCode;
    private String cin;
    private String unitAddress;
    private String unitDistrict;
    private String unitState;
    private String poiCode;

    // Multiple Manufacturing Plants (mapped to vendor_plant)
    private List<SleeperVendorPlantDto> plants;
}
