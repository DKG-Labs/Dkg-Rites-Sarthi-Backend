package com.sarthi.service.finalmaterial;

import com.sarthi.dto.finalmaterial.FinalHardnessTestRequest;
import com.sarthi.dto.finalmaterial.FinalHardnessTestResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Final Hardness Test operations
 * 
 * Handles the two-table design:
 * - Parent table: final_hardness_test (one row per inspection session)
 * - Child table: final_hardness_test_sample (all sample readings)
 * 
 * Supports pausing and resuming inspections.
 */
public interface FinalHardnessTestService {

    /**
     * Save or update a hardness test inspection session with samples.
     * 
     * On FIRST save:
     * - Creates new parent row with status = PENDING
     * - Sets created_by, created_at, updated_by, updated_at
     * 
     * On SUBSEQUENT saves (pause/resume/add samples):
     * - Updates existing parent row
     * - Keeps created_by and created_at unchanged
     * - Updates updated_by and updated_at
     * - Adds new sample rows
     * 
     * @param request The hardness test request with samples
     * @param userId The current user ID
     * @return The saved inspection session with all samples
     */
    FinalHardnessTestResponse saveOrUpdateHardnessTest(FinalHardnessTestRequest request, String userId);

    /**
     * Get hardness test by inspection call, lot, and heat (unique combination).
     */
    Optional<FinalHardnessTestResponse> getHardnessTest(String inspectionCallNo, String lotNo, String heatNo);

    /**
     * Get all hardness tests for an inspection call.
     */
    List<FinalHardnessTestResponse> getHardnessTestsByCall(String inspectionCallNo);

    /**
     * Get hardness test by ID.
     */
    Optional<FinalHardnessTestResponse> getHardnessTestById(Long id);

    /**
     * Update the status of a hardness test (e.g., from PENDING to OK/NOT_OK).
     */
    FinalHardnessTestResponse updateStatus(Long id, String status, String userId);

    /**
     * Update remarks for a hardness test.
     */
    FinalHardnessTestResponse updateRemarks(Long id, String remarks, String userId);

    /**
     * Delete a hardness test and all its samples.
     */
    void deleteHardnessTest(Long id);
}

