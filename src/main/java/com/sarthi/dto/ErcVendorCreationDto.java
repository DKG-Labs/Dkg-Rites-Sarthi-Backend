package com.sarthi.dto;

import lombok.Data;
import java.util.List;

@Data
public class ErcVendorCreationDto {
    private Integer userId;
    private String companyName;
    private String vendorCode;
    private String email;
    private String mobileNumber;
    private String password;
    private String status = "Active";
    private String createdBy;
    private List<ErcVendorUnitDto> units;
}
