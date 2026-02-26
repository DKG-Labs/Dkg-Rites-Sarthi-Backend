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
            ru.rio,
            'PROCESS',
            SUM(q.offered_qty),
            SUM(q.inspected_qty),
            SUM(q.rejected_qty)

        FROM pincode_poi_mapping p

        JOIN ie_poi_mapping ip
             ON ip.poi_code = p.poi_code

        JOIN user_master u
             ON u.userid = ip.ie_user_id

          LEFT JOIN ie_fields_mapping ru
             ON ru.pin_code = p.pin_code

        JOIN process_ie_users pu
             ON pu.ie_user_id = u.userid

        JOIN process_ie_qty q
             ON q.ie_user_id = pu.process_user_id
            WHERE (:startDate IS NULL OR DATE(q.created_date) >= :startDate)
              AND (:endDate IS NULL OR DATE(q.created_date) <= :endDate)
        GROUP BY 
            p.id,
            p.company_name,
            p.poi_code,
            u.username,
            ru.rio
        """,
            countQuery = "SELECT COUNT(*) FROM pincode_poi_mapping",
            nativeQuery = true)
    Page<Object[]> fetchProcess(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,Pageable pageable);
}
