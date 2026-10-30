package com.sarthi.service.scrap;

import com.sarthi.dto.scrap.*;

import java.time.LocalDate;
import java.util.List;

public interface ScrapEntryService {

    List<PlantInfoDto> getMappedPlants(Integer ieUserId, String ieType);

    ScrapLedgerResponseDto getLedger(String poiCode, String unitLineShedNo);

    Integer getPendingQty(String poiCode, LocalDate toDate);



    List<BoardSummaryRowDto> getBoardSummary(String productType);

    RejectionBreakupDto getRejectionBreakup(String poiCode, LocalDate date, String stage, String shift, String lineName);


    ScrapEntryResponseDto addEntry(ScrapEntryRequestDto req);

    ScrapEntryResponseDto updateEntry(Integer id, ScrapEntryUpdateDto req);

    void deleteEntry(Integer id, String reason, String actionBy);
}
