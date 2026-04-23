package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalDepthOfDecarburizationSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Final Inspection - Depth of Decarburization Sample
 */
@Repository
public interface FinalDepthOfDecarburizationSampleRepository extends JpaRepository<FinalDepthOfDecarburizationSample, Long> {

    /**
     * Find all samples for a given parent inspection.
     */
    List<FinalDepthOfDecarburizationSample> findByFinalDepthOfDecarburizationId(Long parentId);

    /**
     * Find samples by parent ID and sampling number.
     */
    List<FinalDepthOfDecarburizationSample> findByFinalDepthOfDecarburizationIdAndSamplingNo(
            Long parentId, Integer samplingNo);

    /**
     * Find samples for multiple parent inspections (Batch).
     */
    List<FinalDepthOfDecarburizationSample> findByFinalDepthOfDecarburizationIdIn(List<Long> parentIds);

    /**
     * Delete all samples for a given parent inspection.
     */
    void deleteByFinalDepthOfDecarburizationId(Long parentId);
}

