package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Final Cumulative Results
 */
@Repository
public interface FinalCumulativeResultsRepository extends JpaRepository<FinalCumulativeResults, Long> {

    /**
     * Find cumulative results by inspection call number
     */
    Optional<FinalCumulativeResults> findByInspectionCallNo(String inspectionCallNo);

    /**
     * Find cumulative results by PO number
     */
    List<FinalCumulativeResults> findByPoNo(String poNo);

    /**
     * Check if cumulative results exist for a call
     */
    boolean existsByInspectionCallNo(String inspectionCallNo);

    @Query(value = """
        SELECT 
            p.id,
            p.company_name,
            p.poi_code,
            u.username,
            ru.rio,
            'FINAL' AS stage,
            SUM(f.qty_now_offered),
            SUM(f.qty_now_passed),
            SUM(f.qty_now_rejected)

        FROM pincode_poi_mapping p

        JOIN ie_pincode_poi_mapping ip
             ON ip.poi_code = p.poi_code

        JOIN user_master u
             ON u.EMPLOYEE_ID = ip.employee_code

        LEFT JOIN ie_fields_mapping ru
             ON ru.pin_code = p.pin_code

        JOIN final_cumulative_results f
             ON f.created_by = u.userid
        WHERE (:startDate IS NULL OR DATE(f.created_at) >= :startDate)
        AND (:endDate IS NULL OR DATE(f.created_at) <= :endDate)

        GROUP BY 
            p.id,
            p.company_name,
            p.poi_code,
            u.username,
            ru.rio
        """,
            countQuery = "SELECT COUNT(*) FROM pincode_poi_mapping",
            nativeQuery = true)
    Page<Object[]> fetchFinal(@Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate, Pageable pageable);
}

