package com.sarthi.Sleeper.dto.Cement;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CementReceiptResponseDto {

    private Long id;

    private String dateOfReceipt;
    private String gradeSpec;
    private String manufacturer;

    private String invoiceNumber;
    private String invoiceDate;

    private Double totalQtyReceived;

    private Integer createdBy;

    private LocalDateTime createdDate;

    private Integer updatedBy;

    private LocalDateTime updatedDate;

    private String status;

    private String vendorCode;
    private String plantId;

    private List<CementBatchDetailsResponseDto> batchDetails;
}