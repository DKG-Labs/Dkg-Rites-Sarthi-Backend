package com.sarthi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryHistoryDto {
    private String inspectionCallNo;
    private LocalDate inspectionDate;
    private BigDecimal offeredQuantity;
    private String inspectionCertificateNo;
    private String certificateUrl;
    private String status;
}
