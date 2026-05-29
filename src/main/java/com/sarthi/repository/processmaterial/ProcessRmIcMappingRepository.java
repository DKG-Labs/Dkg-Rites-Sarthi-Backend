package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessRmIcMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessRmIcMappingRepository extends JpaRepository<ProcessRmIcMapping, Long> {

    /**
     * Find all mappings for a Process IC
     */
    List<ProcessRmIcMapping> findByProcessIcId(Long processIcId);

    /**
     * Find all mappings for an RM IC
     */
    List<ProcessRmIcMapping> findByRmIcId(Long rmIcId);

    /**
     * Find mapping by RM IC Number
     */
    List<ProcessRmIcMapping> findByRmIcNumber(String rmIcNumber);

    /**
     * Delete mappings by Process IC ID
     */
    void deleteByProcessIcId(Long processIcId);
}

