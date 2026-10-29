package com.sarthi.dto.scrap;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScrapLedgerRowDto {
    private String rowType;           // REJECTION or SCRAP
    private Integer entryId;          // null for REJECTION rows
    private LocalDate entryDate;
    private String shift;
    private String unitLineShedNo;
    private String stageOfInspection; // Process / Final
    private Integer rejectedQty;      // null for SCRAP rows
    private Integer scrappedQty;      // null for REJECTION rows
    private Integer pendingBalance;
    private String ieName;
    private String scrapVerificationId;
    private LocalDateTime createdDate;
    private boolean editable;
    private List<String> imageUrls;
    private String remarks;
}
