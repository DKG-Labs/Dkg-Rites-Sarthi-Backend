package com.sarthi.service;

import com.sarthi.dto.RmFinishInspectionDto;

import com.sarthi.dto.RmPreInspectionDataDto;
import com.sarthi.dto.RmHeatFinalResultDto;
import com.sarthi.dto.RmLadleValuesDto;

import java.util.List;


/**
 * Service interface for Raw Material inspection operations.
 */
public interface RmInspectionService {

    /**
     * Save all Raw Material inspection data when inspector finishes inspection.
     * @param dto The complete inspection data payload
     * @param userId The user ID performing the operation
     * @return Success message
     */
    String finishInspection(RmFinishInspectionDto dto, String userId);

    /**
     * Pause Raw Material inspection - saves all submodule data WITHOUT changing status.
     * Called when inspector clicks "Pause Inspection" button.
     * @param dto The complete inspection data payload
     * @param userId The user ID performing the operation
     * @return Success message
     */
    String pauseInspection(RmFinishInspectionDto dto, String userId);

    /**
     * Get all inspection data by call number.
     * @param callNo The inspection call number
     * @return Complete inspection data
     */
    RmFinishInspectionDto getByCallNo(String callNo);


    /**
     * Get cumulative summary data only (pre-inspection data).
     * @param callNo The inspection call number
     * @return Pre-inspection summary data
     */
    RmPreInspectionDataDto getSummaryByCallNo(String callNo);

    /**
     * Get final inspection results for all heats.
     * @param callNo The inspection call number
     * @return List of heat final results
     */
    List<RmHeatFinalResultDto> getFinalResultsByCallNo(String callNo);

    /**
     * Get ladle values (chemical analysis from vendor) for all heats.
     * Used in Material Testing page to display ladle values.
     * @param callNo The inspection call number
     * @return List of ladle values per heat
     */
    List<RmLadleValuesDto> getLadleValuesByCallNo(String callNo);

    /**
     * Get heat details for the vendor dashboard pop-up.
     * @param callNo The inspection call number
     * @return List of heat details (Heat No, TC No, Offered, Accepted, Rejected)
     */
    List<com.sarthi.dto.HeatDetailsDto> getHeatDetailsByCallNo(String callNo);
    List<com.sarthi.dto.HeatDetailsDto> getHeatDetailsByPoSrNo(String poSrNo);

}

