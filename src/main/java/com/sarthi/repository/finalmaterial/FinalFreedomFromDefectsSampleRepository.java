package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalFreedomFromDefectsSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Final Inspection - Freedom from Defects Sample
 */
@Repository
public interface FinalFreedomFromDefectsSampleRepository extends JpaRepository<FinalFreedomFromDefectsSample, Long> {

    /**
     * Find all samples for a given parent inspection.
     */
    List<FinalFreedomFromDefectsSample> findByFinalFreedomFromDefectsTestId(Long parentId);
    
    /**
     * Find samples for multiple parent inspections (Batch).
     */
    List<FinalFreedomFromDefectsSample> findByFinalFreedomFromDefectsTestIdIn(List<Long> parentIds);

    /**
     * Delete all samples for a given parent inspection.
     */
    void deleteByFinalFreedomFromDefectsTestId(Long parentId);
}

