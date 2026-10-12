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

    /**
     * Batch fetch parent RM ICs for multiple Process IC call numbers in a single query (prevents N+1)
     */
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT ic.ic_number AS processCallNo, COALESCE(NULLIF(prim.rm_ic_number, ''), rm_ic.ic_number) AS rmIc
        FROM process_rm_ic_mapping prim
        JOIN inspection_calls ic ON prim.process_ic_id = ic.id
        LEFT JOIN inspection_calls rm_ic ON prim.rm_ic_id = rm_ic.id
        WHERE ic.ic_number IN (:callNos)
        UNION
        SELECT ic.ic_number AS processCallNo, COALESCE(NULLIF(pid.rm_ic_number, ''), rm_ic.ic_number) AS rmIc
        FROM process_inspection_details pid
        JOIN inspection_calls ic ON pid.ic_id = ic.id
        LEFT JOIN inspection_calls rm_ic ON pid.rm_ic_id = rm_ic.id
        WHERE ic.ic_number IN (:callNos)
    """, nativeQuery = true)
    List<Object[]> findBatchRmIcByProcessCallNos(@org.springframework.data.repository.query.Param("callNos") java.util.Collection<String> callNos);
}

