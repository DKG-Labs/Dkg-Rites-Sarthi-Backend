package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalProcessIcMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Final to Process IC Mapping
 */
@Repository
public interface FinalProcessIcMappingRepository extends JpaRepository<FinalProcessIcMapping, Long> {

    /**
     * Find all mappings by Final IC ID
     */
    List<FinalProcessIcMapping> findByFinalIcId(Long finalIcId);

    /**
     * Find all mappings by Process IC ID
     */
    List<FinalProcessIcMapping> findByProcessIcId(Long processIcId);

    /**
     * Find all mappings by Process IC Number
     */
    List<FinalProcessIcMapping> findByProcessIcNumber(String processIcNumber);

    /**
     * Delete mappings by Final IC ID
     */
    void deleteByFinalIcId(Long finalIcId);
}

