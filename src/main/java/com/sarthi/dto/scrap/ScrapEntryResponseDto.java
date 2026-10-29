package com.sarthi.dto.scrap;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScrapEntryResponseDto {
    private Integer id;
    private String poiCode;
    private String unitLineShedNo;
    private LocalDate scrappingDate;
    private String shift;
    private Integer noOfPiecesScrapped;
    private Integer pendingQtyAtSubmit;
    private Boolean declarationAccepted;
    private String remarks;
    private String scrapVerificationId;
    private Integer ieUserId;
    private String ieType;
    private String status;
    private String createdBy;
    private LocalDateTime createdDate;
    private String updatedBy;
    private LocalDateTime updatedDate;
    private List<String> imageUrls;
    private boolean editable;
}
