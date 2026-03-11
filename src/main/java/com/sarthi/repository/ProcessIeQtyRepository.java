package com.sarthi.repository;

import com.sarthi.dto.InspectionQtySummaryView;
import com.sarthi.dto.TotalManufaturedQtyOfPoDto;
import com.sarthi.entity.ProcessIeQty;
import org.springframework.beans.PropertyValues;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessIeQtyRepository
        extends JpaRepository<ProcessIeQty, Long> {

    Optional<ProcessIeQty> findByRequestIdAndSwiftCode(
            String requestId, String swiftCode);

    @Query("SELECT COALESCE(SUM(p.inspectedQty),0) " +
            "FROM ProcessIeQty p WHERE p.requestId = :requestId")
    int sumInspectedQtyByRequestId(@Param("requestId") String requestId);

    @Query("""
                SELECT COALESCE(SUM(p.inspectedQty), 0)
                FROM ProcessIeQty p
                WHERE p.requestId = :requestId
                  AND p.lotNumber = :lotNumber
            """)
    int sumInspectedQtyByRequestIdAndLotNumber(
            @Param("requestId") String requestId,
            @Param("lotNumber") String lotNumber);

    @Query("""
                SELECT COALESCE(SUM(p.inspectedQty), 0)
                FROM ProcessIeQty p
                WHERE p.requestId = :requestId
                  AND p.lotNumber = :lotNumber
                  AND p.heatNo = :heatNo
            """)
    int sumInspectedQtyByRequestIdAndLotNumberAndHeatNo(
            @Param("requestId") String requestId,
            @Param("lotNumber") String lotNumber,
            @Param("heatNo") String heatNo);

    @Query("""
                SELECT COALESCE(MAX(p.offeredQty), 0)
                FROM ProcessIeQty p
                WHERE p.requestId = :requestId
                  AND p.lotNumber = :lotNumber
            """)
    int findOfferedQtyByRequestIdAndLotNumber(
            @Param("requestId") String requestId,
            @Param("lotNumber") String lotNumber);

    /*
     * @Query("""
     * SELECT
     * COALESCE(SUM(p.inspectedQty), 0) AS acceptedQty,
     * COALESCE(SUM(p.offeredQty), 0) AS totalOfferedQty,
     * COALESCE(SUM(p.manufactureQty), 0) AS totalManufactureQty,
     * COALESCE(SUM(p.rejectedQty), 0) AS totalRejectedQty
     * FROM ProcessIeQty p
     * WHERE p.requestId = :requestId
     * """)
     * InspectionQtySummaryView getQtySummaryByRequestId(
     * 
     * @Param("requestId") String requestId
     * );
     */
    @Query("""
                SELECT
                    p.lotNumber AS lotNumber,
                    COALESCE(SUM(p.inspectedQty), 0)   AS acceptedQty,
                    COALESCE(SUM(p.manufactureQty), 0) AS manufacturedQty,
                    COALESCE(SUM(p.rejectedQty), 0)    AS rejectedQty,
                    COALESCE(SUM(p.offeredQty), 0)    AS offeredQty
                FROM ProcessIeQty p
                WHERE p.requestId = :requestId
                GROUP BY p.lotNumber
            """)
    List<InspectionQtySummaryView> getLotWiseQtySummary(
            @Param("requestId") String requestId);

    boolean existsByRequestId(String requestId);

    /*
     * @Query("""
     * SELECT COALESCE(SUM(p.manufactureQty), 0)
     * FROM ProcessIeQty p
     * WHERE p.requestId IN :callNos
     * AND (:heatNo IS NULL OR p.heatNo = :heatNo)
     * """)
     * BigDecimal sumManufacturedQtyByCallNos(
     * 
     * @Param("callNos") List<String> callNos,
     * 
     * @Param("heatNo") String heatNo
     * );
     */
    @Query("""
                SELECT new com.sarthi.dto.TotalManufaturedQtyOfPoDto(
                    CAST(COALESCE(SUM(p.manufactureQty), 0) AS big_decimal),
                    COALESCE(SUM(p.rejectedQty), 0),
                    NULL,
                    CAST(COALESCE(SUM(p.inspectedQty), 0) AS big_decimal),
                    :heatNo,
                    NULL
                )
                FROM ProcessIeQty p
                WHERE p.requestId IN :callNos
                AND p.heatNo = :heatNo
            """)
    TotalManufaturedQtyOfPoDto sumProcessQty(
            @Param("callNos") List<String> callNos,
            @Param("heatNo") String heatNo);

    @Query("""
                SELECT
                    CASE
                        WHEN SUM(p.offeredQty) = 0 THEN 0.0
                        ELSE (SUM(p.rejectedQty) * 100.0) / SUM(p.offeredQty)
                    END
                FROM ProcessIeQty p
                WHERE p.requestId IN :callNos
            """)
    Double findProcessRejectionPctByCallNos(
            @Param("callNos") List<String> callNos);

    @Query("""
                SELECT
                    SUM(p.inspectedQty),
                    SUM(p.rejectedQty),
                    SUM(p.offeredQty)
                FROM ProcessIeQty p
                WHERE p.requestId IN :callNos
            """)
    List<Object[]> findProcessSummaryByCallNos(
            @Param("callNos") List<String> callNos);

    @Query("""
                SELECT
                    SUM(p.offeredQty),
                    SUM(p.inspectedQty),
                    SUM(p.rejectedQty)
                FROM ProcessIeQty p
                WHERE p.requestId = :callNo
            """)
    List<Object[]> findProcessQtyByCallNo(
            @Param("callNo") String callNo);

    @Query("""
                SELECT
                    p.requestId,
                    SUM(p.offeredQty),
                    SUM(p.inspectedQty),
                    SUM(p.rejectedQty)
                FROM ProcessIeQty p
                WHERE p.requestId IN :callNos
                GROUP BY p.requestId
            """)
    List<Object[]> findProcessQtyByCallNos(
            @Param("callNos") List<String> callNos);

    @Query("SELECT SUM(p.rejectedQty), SUM(p.offeredQty) FROM ProcessIeQty p WHERE p.createdDate >= :date")
    List<Object[]> sumProcessRejectionLast30Days(@Param("date") java.util.Date date);

    @Query(value = """
            SELECT
                p.id,
                p.company_name,
                p.poi_code,
                u.username,
                ip.rio,
                'PROCESS',
                SUM(q.offered_qty),
                SUM(q.inspected_qty),
                SUM(q.rejected_qty)

            FROM process_ie_qty q
            JOIN inspection_calls ic ON ic.ic_number = q.request_id
            JOIN pincode_poi_mapping p ON p.poi_code = ic.place_of_inspection
            LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
            LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
            LEFT JOIN po_header ph ON ph.po_no = ic.po_no

            JOIN user_master u ON u.userid = q.ie_user_id

                WHERE (:startDate IS NULL OR DATE(q.created_date) >= :startDate)
                  AND (:endDate IS NULL OR DATE(q.created_date) <= :endDate)
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
    Page<Object[]> fetchProcess(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rio") String rio,
            @Param("zone") String zone,
            @Param("vendor") String vendor,
            Pageable pageable);

    @Query(value = """
            SELECT
                DATE(p.date_of_inspection) AS inspectionDate,
                p.SWIFT_CODE AS shift,

                SUM(p.INSPECTED_QTY) AS accepted,
                SUM(p.rejected_qty) AS rejected,

                COALESCE(SUM(
                    s.length_cut_bar_rejected +
                    s.improper_dia_rejected +
                    s.sharp_edges_rejected +
                    s.cracked_edges_rejected
                ),0) AS shearing,

                COALESCE(SUM(
                    t.parallel_length_rejected +
                    t.full_turning_length_rejected +
                    t.turning_dia_rejected
                ),0) AS turning,

                COALESCE(SUM(temp.total_tempering_rejection),0) AS tempering,
                COALESCE(SUM(mpi.mpi_rejected),0) AS mpi,

                COALESCE(SUM(
                    forg.forging_temp_rejected +
                    forg.improper_forging_rejected +
                    forg.forging_defect_rejected +
                    forg.embossing_defect_rejected
                ),0) AS forging,

                COALESCE(SUM(
                    q.quenching_duration_rejected +
                    q.quenching_hardness_rejected +
                    q.box_gauge_rejected +
                    q.flat_bearing_area_rejected +
                    q.falling_gauge_rejected
                ),0) AS quenching

            FROM process_ie_qty p

            LEFT JOIN process_shearing_data s
                   ON s.inspection_call_no = p.REQUEST_ID
                  AND s.lot_no = p.lot_number
                  AND s.shift = p.SWIFT_CODE

            LEFT JOIN process_turning_data t
                   ON t.inspection_call_no = p.REQUEST_ID
                  AND t.lot_no = p.lot_number
                  AND t.shift = p.SWIFT_CODE

            LEFT JOIN process_tempering_data temp
                   ON temp.inspection_call_no = p.REQUEST_ID
                  AND temp.lot_no = p.lot_number
                  AND temp.shift = p.SWIFT_CODE

            LEFT JOIN process_mpi_data mpi
                   ON mpi.inspection_call_no = p.REQUEST_ID
                  AND mpi.lot_no = p.lot_number
                  AND mpi.shift = p.SWIFT_CODE

            LEFT JOIN process_forging_data forg
                   ON forg.inspection_call_no = p.REQUEST_ID
                  AND forg.lot_no = p.lot_number
                  AND forg.shift = p.SWIFT_CODE

            LEFT JOIN process_quenching_data q
                   ON q.inspection_call_no = p.REQUEST_ID
                  AND q.lot_no = p.lot_number
                  AND q.shift = p.SWIFT_CODE

            WHERE p.REQUEST_ID = :callNo
              AND p.lot_number = :lotNo

            GROUP BY DATE(p.date_of_inspection), p.SWIFT_CODE
            ORDER BY inspectionDate
            """, nativeQuery = true)
    List<Object[]> getLotClosedLoop(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo);

    @Query(value = """
            SELECT DISTINCT p.REQUEST_ID
            FROM process_ie_qty p
            WHERE DATE(p.date_of_inspection) BETWEEN :startDate AND :endDate
            ORDER BY p.REQUEST_ID
            """, nativeQuery = true)
    List<String> findRequestIdsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = """
            SELECT DISTINCT p.lot_number
            FROM process_ie_qty p
            WHERE p.REQUEST_ID = :requestId
            ORDER BY p.lot_number
            """, nativeQuery = true)
    List<String> findLotNumbersByRequestId(
            @Param("requestId") String requestId);
}
