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

    /**
     * Batch fetch parent Process ICs for multiple Final IC call numbers in a single query (prevents N+1)
     */
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT ic.ic_number AS finalCallNo, COALESCE(NULLIF(fpim.process_ic_number, ''), proc_ic.ic_number) AS processIc
        FROM final_process_ic_mapping fpim
        JOIN inspection_calls ic ON fpim.final_ic_id = ic.id
        LEFT JOIN inspection_calls proc_ic ON fpim.process_ic_id = proc_ic.id
        WHERE ic.ic_number IN (:callNos)
        UNION
        SELECT ic.ic_number AS finalCallNo, COALESCE(NULLIF(fid.process_ic_number, ''), proc_ic.ic_number) AS processIc
        FROM final_inspection_details fid
        JOIN inspection_calls ic ON fid.ic_id = ic.id
        LEFT JOIN inspection_calls proc_ic ON fid.process_ic_id = proc_ic.id
        WHERE ic.ic_number IN (:callNos)
    """, nativeQuery = true)
    List<Object[]> findBatchProcessIcByFinalCallNos(@org.springframework.data.repository.query.Param("callNos") java.util.Collection<String> callNos);
}

