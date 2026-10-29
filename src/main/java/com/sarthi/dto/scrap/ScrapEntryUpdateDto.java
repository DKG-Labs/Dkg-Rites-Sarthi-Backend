package com.sarthi.dto.scrap;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScrapEntryUpdateDto {
    private LocalDate scrappingDate;
    private String shift;
    private Integer noOfPiecesScrapped;
    private String remarks;
    private String reason;
    private String actionBy;
    private List<ScrapEntryRequestDto.ScrapImageDto> images;
}
