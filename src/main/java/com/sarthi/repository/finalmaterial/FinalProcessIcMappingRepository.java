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
          AND ic.ic_number NOT LIKE 'ER%'
          AND ic.ic_number NOT LIKE '%/ER%'
          AND ic.ic_number NOT LIKE '%ER-%'
        UNION
        SELECT ic.ic_number AS finalCallNo, COALESCE(NULLIF(fid.process_ic_number, ''), proc_ic.ic_number) AS processIc
        FROM final_inspection_details fid
        JOIN inspection_calls ic ON fid.ic_id = ic.id
        LEFT JOIN inspection_calls proc_ic ON fid.process_ic_id = proc_ic.id
        WHERE ic.ic_number IN (:callNos)
          AND ic.ic_number NOT LIKE 'ER%'
          AND ic.ic_number NOT LIKE '%/ER%'
          AND ic.ic_number NOT LIKE '%ER-%'
        UNION
        SELECT ic.ic_number AS finalCallNo, COALESCE(NULLIF(fpim.process_ic_number, ''), proc_ic.ic_number) AS processIc
        FROM inspection_calls ic
        JOIN inspection_calls src ON (
            src.ic_number COLLATE utf8mb4_unicode_ci = ic.source_ic_no COLLATE utf8mb4_unicode_ci
            OR ic.source_ic_no COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', src.ic_number, '%')
            OR src.ic_number COLLATE utf8mb4_unicode_ci IN (SELECT icd_sub.call_no FROM inspection_complete_details icd_sub WHERE icd_sub.certificate_no COLLATE utf8mb4_unicode_ci = ic.source_ic_no COLLATE utf8mb4_unicode_ci)
        )
        JOIN final_process_ic_mapping fpim ON fpim.final_ic_id = src.id
        LEFT JOIN inspection_calls proc_ic ON fpim.process_ic_id = proc_ic.id
        WHERE ic.ic_number IN (:callNos)
          AND ic.ic_number NOT LIKE 'ER%'
          AND ic.ic_number NOT LIKE '%/ER%'
          AND ic.ic_number NOT LIKE '%ER-%'
          AND (ic.is_diverted_material = 1 OR (ic.source_ic_no IS NOT NULL AND ic.source_ic_no <> ''))
        UNION
        SELECT ic.ic_number AS finalCallNo, COALESCE(NULLIF(fid.process_ic_number, ''), proc_ic.ic_number) AS processIc
        FROM inspection_calls ic
        JOIN inspection_calls src ON (
            src.ic_number COLLATE utf8mb4_unicode_ci = ic.source_ic_no COLLATE utf8mb4_unicode_ci
            OR ic.source_ic_no COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', src.ic_number, '%')
            OR src.ic_number COLLATE utf8mb4_unicode_ci IN (SELECT icd_sub.call_no FROM inspection_complete_details icd_sub WHERE icd_sub.certificate_no COLLATE utf8mb4_unicode_ci = ic.source_ic_no COLLATE utf8mb4_unicode_ci)
        )
        JOIN final_inspection_details fid ON fid.ic_id = src.id
        LEFT JOIN inspection_calls proc_ic ON fid.process_ic_id = proc_ic.id
        WHERE ic.ic_number IN (:callNos)
          AND ic.ic_number NOT LIKE 'ER%'
          AND ic.ic_number NOT LIKE '%/ER%'
          AND ic.ic_number NOT LIKE '%ER-%'
          AND (ic.is_diverted_material = 1 OR (ic.source_ic_no IS NOT NULL AND ic.source_ic_no <> ''))
        UNION
        SELECT ic.ic_number AS finalCallNo, ic.source_ic_no AS processIc
        FROM inspection_calls ic
        WHERE ic.ic_number IN (:callNos)
          AND ic.ic_number NOT LIKE 'ER%'
          AND ic.ic_number NOT LIKE '%/ER%'
          AND ic.ic_number NOT LIKE '%ER-%'
          AND (ic.is_diverted_material = 1 OR (ic.source_ic_no IS NOT NULL AND ic.source_ic_no <> ''))
          AND (ic.source_ic_no LIKE '%EP-%' OR ic.source_ic_no LIKE '%/EP%')
    """, nativeQuery = true)
    List<Object[]> findBatchProcessIcByFinalCallNos(@org.springframework.data.repository.query.Param("callNos") java.util.Collection<String> callNos);
}

