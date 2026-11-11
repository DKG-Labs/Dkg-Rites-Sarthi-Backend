package com.sarthi.udmcomplaint.dto;

import lombok.Data;

@Data
public class UdmComplaintDocumentDto {
    private Long id;
    private String stage;
    private Integer roundNo;
    private String docType;
    private String fileName;
    private Long sizeBytes;
    private Integer uploadedBy;
    private String uploadedAt;
}
