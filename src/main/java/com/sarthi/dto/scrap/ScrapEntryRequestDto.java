package com.sarthi.dto.scrap;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScrapEntryRequestDto {
    private String poiCode;
    private String unitLineShedNo;
    private LocalDate scrappingDate;
    private String shift;
    private Integer noOfPiecesScrapped;
    private Boolean declarationAccepted;
    private String remarks;
    private Integer ieUserId;
    private String ieType;
    private String actionBy;
    private List<ScrapImageDto> images;

    @Data
    public static class ScrapImageDto {
        private String base64Data;
        private String fileName;
    }
}
