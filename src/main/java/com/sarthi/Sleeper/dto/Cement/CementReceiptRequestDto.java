package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;

import java.util.List;

@Data
public class CementReceiptRequestDto {

    private String dateOfReceipt;
    private String gradeSpec;
    private String manufacturer;

    private String invoiceNumber;
    private String invoiceDate;

    private Integer createdBy;
    private Integer updatedBy;

    private String vendorCode;
    private String plantId;

    private List<CementBatchDetailsRequestDto> batchDetails;
}