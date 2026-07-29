package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalInspectionLotResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Final Inspection Lot Results
 */
@Repository
public interface FinalInspectionLotResultsRepository extends JpaRepository<FinalInspectionLotResults, Long> {

    /**
     * Find lot results by inspection call number and lot number
     */
    Optional<FinalInspectionLotResults> findByInspectionCallNoAndLotNo(String inspectionCallNo, String lotNo);

    /**
     * Find all lot results for an inspection call
     */
    List<FinalInspectionLotResults> findByInspectionCallNo(String inspectionCallNo);

    /**
     * Sum erc_used_for_testing across all lots for an inspection call
     */
    @Query("SELECT COALESCE(SUM(f.ercUsedForTesting), 0) FROM FinalInspectionLotResults f WHERE f.inspectionCallNo = :inspectionCallNo")
    Integer sumErcUsedForTestingByInspectionCallNo(@Param("inspectionCallNo") String inspectionCallNo);

    /**
     * Sum total_rejected_qty across all lots for an inspection call
     */
    @Query("SELECT COALESCE(SUM(f.totalRejectedQty), 0) FROM FinalInspectionLotResults f WHERE f.inspectionCallNo = :inspectionCallNo")
    Integer sumTotalRejectedQtyByInspectionCallNo(@Param("inspectionCallNo") String inspectionCallNo);

    /**
     * Find lot results by lot number
     */
    List<FinalInspectionLotResults> findByLotNo(String lotNo);

    /**
     * Check if lot results exist
     */
    boolean existsByInspectionCallNoAndLotNo(String inspectionCallNo, String lotNo);

    @Query("""
SELECT
    r.inspectionCallNo,

    MAX(
        CASE
            WHEN UPPER(r.visualDimStatus) = 'NOT OK'
            THEN 1
            ELSE 0
        END
    ),

    MAX(
        CASE
            WHEN UPPER(r.hardnessStatus) = 'NOT OK'
            THEN 1
            ELSE 0
        END
    ),

    MAX(
        CASE
            WHEN UPPER(r.inclusionStatus) = 'NOT OK'
            THEN 1
            ELSE 0
        END
    ),

    MAX(
        CASE
            WHEN UPPER(r.deflectionStatus) = 'NOT OK'
            THEN 1
            ELSE 0
        END
    ),

    MAX(
        CASE
            WHEN UPPER(r.toeLoadStatus) = 'NOT OK'
            THEN 1
            ELSE 0
        END
    )

FROM FinalInspectionLotResults r

WHERE r.inspectionCallNo IN :callNos

GROUP BY r.inspectionCallNo
""")
    List<Object[]> getFinalDefectSummary(
            @Param("callNos") List<String> callNos);
}

