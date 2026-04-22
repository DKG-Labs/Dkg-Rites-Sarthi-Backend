package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalInclusionRatingSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Final Inspection - Inclusion Rating Sample
 */
@Repository
public interface FinalInclusionRatingSampleRepository extends JpaRepository<FinalInclusionRatingSample, Long> {

    /**
     * Find all samples for a given parent inspection.
     */
    List<FinalInclusionRatingSample> findByFinalInclusionRatingId(Long parentId);

    /**
     * Find samples by parent ID and sampling number.
     */
    List<FinalInclusionRatingSample> findByFinalInclusionRatingIdAndSamplingNo(
            Long parentId, Integer samplingNo);

    /**
     * Find samples for multiple parent inspections (Batch).
     */
    List<FinalInclusionRatingSample> findByFinalInclusionRatingIdIn(List<Long> parentIds);

    /**
     * Delete all samples for a given parent inspection.
     */
    void deleteByFinalInclusionRatingId(Long parentId);
}

