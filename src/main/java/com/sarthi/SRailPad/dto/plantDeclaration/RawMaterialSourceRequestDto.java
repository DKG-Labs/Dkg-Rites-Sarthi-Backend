package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RawMaterialSourceRequestDto {
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private String materialName;
    private String materialType;
    private String supplierName;
    private String docRefNo;
    private LocalDate docDate;
    private Long createdBy;
    private Long updatedBy;
}
