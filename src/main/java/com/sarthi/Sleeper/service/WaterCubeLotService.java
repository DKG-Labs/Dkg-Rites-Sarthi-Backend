package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthTestRequestDto;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import com.sarthi.Sleeper.entity.ProductionDeclaration.LotRejectionTestType;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Water cube lot acceptance: evaluates a test on the server and, when the lot fails, rejects the entire batch
 * (returned to vendor, every sleeper rejected).
 */
public interface WaterCubeLotService {

    /** Server-side result of a water cube test (overrides whatever the client computed). */
    record Evaluation(Double fck,
                      Double s1Avg, Double s2Avg,
                      Double avgX, Double minY,
                      Double s1Variation, Double s2Variation,
                      boolean condition1, boolean condition2, boolean condition3,
                      int mrSamplesRequired,
                      String finalTestResult,
                      boolean failed) {
    }

    /** Outcome of rejecting a lot: the batch and the inspection calls (not cancelled/withdrawn) that still carry it. */
    record Rejection(Long productionDeclarationId, String batchNumber, List<String> affectedCallNos) {
    }

    /** Recomputes averages, variations, conditions and PASS/FAIL from the cube strengths. */
    Evaluation evaluate(WaterCubeStrengthTestRequestDto dto);

    /** Rejects the batch of the given (saved) failed test. */
    Rejection rejectLot(WaterCubeStrengthTest test, Long fallbackProductionDeclarationId, Long userId);

    /**
     * Rejects the production batch (lot) with this id because the given test failed: returned to vendor, every
     * sleeper rejected. An already rejected batch keeps its original rejection.
     */
    Rejection rejectBatch(Long productionDeclarationId, String reason, LotRejectionTestType testType, Long testId, Long userId);

    boolean isRejected(ProductionDeclaration declaration);

    /** True when a stored final_test_result is the configured pass value (case-insensitive, prefix match like before). */
    boolean isPassResult(String finalTestResult);

    /** True when this water cube test rejected a batch; such a test may not be changed or deleted. */
    boolean isRejectingTest(Long testId);

    /** True when this test (of the given type) rejected a batch; such a test may not be changed or deleted. */
    boolean isRejectingTest(LotRejectionTestType testType, Long testId);

    /** Throws when the batch (of the plant) has been rejected. Blank batch numbers are ignored. */
    void assertNotRejected(String batchNumber, String plantId);

    /** Throws when the production batch with this id has been rejected. A null id is ignored. */
    void assertNotRejected(Long productionDeclarationId);

    /** Rejected batch ids among the given ids. */
    Set<Long> findRejectedIds(Collection<Long> batchIds);
}
