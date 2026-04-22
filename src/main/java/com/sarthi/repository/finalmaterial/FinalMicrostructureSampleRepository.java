package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalMicrostructureSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Final Inspection - Microstructure Sample
 */
@Repository
public interface FinalMicrostructureSampleRepository extends JpaRepository<FinalMicrostructureSample, Long> {

    /**
     * Find all samples for a given parent inspection.
     */
    List<FinalMicrostructureSample> findByFinalMicrostructureTestId(Long parentId);
    
    /**
     * Find samples for multiple parent inspections (Batch).
     */
    List<FinalMicrostructureSample> findByFinalMicrostructureTestIdIn(List<Long> parentIds);

    /**
     * Delete all samples for a given parent inspection.
     */
    void deleteByFinalMicrostructureTestId(Long parentId);
}

