package com.sarthi.service;

import com.sarthi.dto.processmaterial.ProcessFinishInspectionDto;
import com.sarthi.dto.processmaterial.ProcessLineFinalResultDto;

import java.util.List;

/**
 * Service interface for Process Material inspection operations.
 */
public interface ProcessInspectionService {

    /**
     * Save all Process Material inspection data when inspector finishes inspection.
     * Handles both finish and pause operations.
     * @param dto The complete inspection data payload
     * @param userId The user ID performing the operation
     * @return Success message
     */
    String finishInspection(ProcessFinishInspectionDto dto, String userId);

    /**
     * Save/Update inspection data when user clicks pause.
     * Saves all entered data without changing inspection call status.
     * @param dto The inspection data payload
     * @param userId The user ID performing the operation
     * @return Success message
     */
    String pauseInspection(ProcessFinishInspectionDto dto, String userId);

    /**
     * Get all inspection data by call number.
     * Used when revisiting an inspection call to fetch all previously saved data.
     * @param callNo The inspection call number
     * @return Complete inspection data
     */
    ProcessFinishInspectionDto getByCallNo(String callNo);

    /**
     * Get final inspection results for all production lines.
     * @param callNo The inspection call number
     * @return List of line final results
     */
    /**
     * Get final inspection results for all production lines for a given inspection call.
     * @param callNo The inspection call number
     * @return List of line final results
     */
    List<ProcessLineFinalResultDto> getFinalResultsByCallNo(String callNo);

    /**
     * Get sum of accepted quantities for all stages by call number and lot number.
     * @param callNo The inspection call number
     * @param lotNo The lot number
     * @return DTO containing summed accepted quantities
     */
    com.sarthi.dto.processmaterial.ProcessStageAcceptedQtyDto getAcceptedQuantitySum(String callNo, String lotNo);
}

