package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RawMaterialSourceResponseDto {
    private Long id;
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private String materialName;
    private String materialType;
    private String supplierName;
    private String docRefNo;
    private LocalDate docDate;
    private String status;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
