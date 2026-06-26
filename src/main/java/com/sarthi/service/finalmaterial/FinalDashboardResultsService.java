package com.sarthi.service.finalmaterial;

import com.sarthi.dto.finalmaterial.FinalCumulativeResultsDto;
import com.sarthi.dto.finalmaterial.FinalInspectionSummaryDto;
import com.sarthi.dto.finalmaterial.FinalInspectionLotResultsDto;
import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import com.sarthi.entity.finalmaterial.FinalInspectionSummary;
import com.sarthi.entity.finalmaterial.FinalInspectionLotResults;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Final Dashboard Results
 * Handles saving and retrieving cumulative results and inspection summary data
 */
public interface FinalDashboardResultsService {

    // ===== CUMULATIVE RESULTS =====
    FinalCumulativeResults saveCumulativeResults(FinalCumulativeResultsDto dto, String userId);
    Optional<FinalCumulativeResults> getCumulativeResultsByCallNo(String inspectionCallNo);
    List<FinalCumulativeResults> getCumulativeResultsByPoNo(String poNo);
    FinalCumulativeResults updateCumulativeResults(FinalCumulativeResultsDto dto, String userId);
    void deleteCumulativeResults(String inspectionCallNo);

    // ===== INSPECTION SUMMARY =====
    FinalInspectionSummary saveInspectionSummary(FinalInspectionSummaryDto dto, String userId);
    Optional<FinalInspectionSummaryDto> getInspectionSummaryByCallNo(String inspectionCallNo);
    FinalInspectionSummary updateInspectionSummary(FinalInspectionSummaryDto dto, String userId);
    void deleteInspectionSummary(String inspectionCallNo);

    // ===== LOT RESULTS =====
    FinalInspectionLotResults saveLotResults(FinalInspectionLotResultsDto dto, String userId);
    Optional<FinalInspectionLotResults> getLotResultsByCallNoAndLotNo(String inspectionCallNo, String lotNo);
    List<FinalInspectionLotResults> getLotResultsByCallNo(String inspectionCallNo);
    List<FinalInspectionLotResults> getLotResultsByLotNo(String lotNo);
    FinalInspectionLotResults updateLotResults(FinalInspectionLotResultsDto dto, String userId);
    void deleteLotResults(String inspectionCallNo, String lotNo);
}

