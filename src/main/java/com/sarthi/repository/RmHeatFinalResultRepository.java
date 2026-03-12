package com.sarthi.repository;

import com.sarthi.entity.RmHeatFinalResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for RmHeatFinalResult entity.
 */
@Repository
public interface RmHeatFinalResultRepository extends JpaRepository<RmHeatFinalResult, Long> {

    List<RmHeatFinalResult> findByInspectionCallNo(String inspectionCallNo);

    List<RmHeatFinalResult> findByInspectionCallNoAndHeatNo(String inspectionCallNo, String heatNo);

    List<RmHeatFinalResult> findByInspectionCallNoInAndHeatNo(List<String> inspectionCallNos, String heatNo);

    void deleteByInspectionCallNo(String inspectionCallNo);

    @Query("""
                SELECT COALESCE(SUM(r.acceptedQtyMt), 0)
                FROM RmHeatFinalResult r
                WHERE r.inspectionCallNo IN :callNos
                AND r.heatNo = :heatNo
            """)
    BigDecimal sumRmAcceptedQty(
            @Param("callNos") List<String> callNos,
            @Param("heatNo") String heatNo);

    @Query("""
                SELECT COALESCE(SUM(r.weightAcceptedMt), 0)
                FROM RmHeatFinalResult r
                WHERE r.inspectionCallNo IN :callNos
                AND r.heatNo = :heatNo
            """)
    BigDecimal sumWeightAcceptedMt(
            @Param("callNos") List<String> callNos,
            @Param("heatNo") String heatNo);

    /**
     * Sum offered earlier for a heat across multiple inspection calls
     */
    // @Query("""
    // SELECT COALESCE(SUM(r.offeredEarlier), 0)
    // FROM RmHeatFinalResult r
    // WHERE r.inspectionCallNo IN :callNos
    // AND r.heatNo = :heatNo
    // """)
    // Integer sumOfferedEarlierByHeatNoAndInspectionCallNos(
    // @Param("heatNo") String heatNo,
    // @Param("callNos") List<String> callNos
    // );

    @Query("""
                SELECT
                    SUM(r.totalQtyOfferedMt),
                    SUM(r.weightRejectedMt)
                FROM RmHeatFinalResult r
                WHERE r.inspectionCallNo IN :callNos
            """)
    List<Object[]> findOfferedAndRejectedByCallNos(
            @Param("callNos") List<String> callNos);

    /*
     * @Query("""
     * SELECT
     * SUM(r.acceptedQtyMt),
     * SUM(r.weightRejectedMt),
     * SUM(r.weightOfferedMt)
     * FROM RmHeatFinalResult r
     * WHERE r.inspectionCallNo IN :callNos
     * """)
     * List<Object[]> findRmSummaryByCallNos(
     * 
     * @Param("callNos") List<String> callNos
     * );
     */
    /*
     * @Query("""
     * SELECT
     * r.inspectionCallNo,
     * SUM(r.totalQtyOfferedMt),
     * SUM(r.weightAcceptedMt),
     * SUM(r.weightRejectedMt)
     * FROM RmHeatFinalResult r
     * WHERE r.inspectionCallNo IN :callNos
     * GROUP BY r.inspectionCallNo
     * """)
     * List<Object[]> findRmSummaryByCallNos(
     * 
     * @Param("callNos") List<String> callNos
     * );
     */
    @Query("""
            SELECT
                r.inspectionCallNo,
                SUM(r.totalQtyOfferedMt),
                SUM(r.weightAcceptedMt),
                SUM(r.weightRejectedMt)
            FROM RmHeatFinalResult r
            WHERE r.inspectionCallNo IN :callNos
            GROUP BY r.inspectionCallNo
            """)
    List<Object[]> findRmSummaryByCallNos(
            @Param("callNos") List<String> callNos);

    @Query(value = """
            SELECT
                p.id,
                p.company_name,
                p.poi_code,
                u.username,
                ip.rio,
                'Raw Material' AS stage,
                SUM(r.total_qty_offered_mt),
                SUM(r.accepted_qty_mt),
                SUM(r.weight_rejected_mt)
            FROM rm_heat_final_result r
            JOIN inspection_calls ic ON ic.ic_number = r.inspection_call_no
            JOIN pincode_poi_mapping p ON p.poi_code = ic.place_of_inspection
            LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
            LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
            LEFT JOIN po_header ph ON ph.po_no = ic.po_no

            JOIN user_master u ON u.userid = r.created_by

            WHERE (:startDate IS NULL OR DATE(r.created_at) >= :startDate)
              AND (:endDate IS NULL OR DATE(r.created_at) <= :endDate)
              AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
              AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
              AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)
            GROUP BY
                p.id,
                p.company_name,
                p.poi_code,
                u.username,
                ip.rio
            """, countQuery = "SELECT COUNT(*) FROM pincode_poi_mapping", nativeQuery = true)
    Page<Object[]> fetchRaw(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rio") String rio,
            @Param("zone") String zone,
            @Param("vendor") String vendor,
            Pageable pageable);

    @Query("SELECT SUM(r.weightRejectedMt), SUM(r.weightOfferedMt) FROM RmHeatFinalResult r WHERE r.createdAt >= :date")
    List<Object[]> sumRmRejectionLast30Days(@Param("date") java.time.LocalDateTime date);

}
