package com.sarthi.repository;

import com.sarthi.dto.reports.PoInspection1stLevelStatusDto;
import com.sarthi.entity.PoHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoHeaderRepository extends JpaRepository<PoHeader, Long> {

    /**
     * Find PO Header by PO Number.
     */
    Optional<PoHeader> findByPoNo(String poNo);

    List<PoHeader> findByPoNoIn(List<String> poNos);

    /**
     * Find PO Header by PO Number with items eagerly loaded (JOIN FETCH).
     * Use this when item data is needed to avoid LazyInitializationException.
     */
    @Query("SELECT DISTINCT h FROM PoHeader h LEFT JOIN FETCH h.items WHERE h.poNo = :poNo")
    Optional<PoHeader> findByPoNoWithItems(@Param("poNo") String poNo);

    boolean existsByPoKey(String poKey);

    List<PoHeader> findByVendorCode(String vendorCode);

    // @Query("""
    // select distinct h
    // from PoHeader h
    // left join fetch h.items i
    // where h.vendorCode = :vendorCode
    // """)
    // List<PoHeader> findAllByVendorCodeWithItems(String vendorCode);
    @Query("""
                select distinct h
                from PoHeader h
                left join fetch h.items i
                where h.vendorCode = :vendorCode
            """)
    List<PoHeader> findAllByVendorCodeWithItems(String vendorCode);

    /*
     * @Query("""
     * SELECT new com.sarthi.dto.reports.PoInspection1stLevelStatusDto(
     * 0,
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * SUM(pi.qty),
     * null,
     * null,
     * null,
     * null,
     * null,
     * ph.poStatus
     * )
     * FROM PoHeader ph
     * JOIN ph.items pi
     * GROUP BY
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * ph.poStatus
     * """)
     * List<PoInspection1stLevelStatusDto> fetchPoInspectionStatus();
     */
    /*
     * @Query("""
     * SELECT new com.sarthi.dto.reports.PoInspection1stLevelStatusDto(
     * 0,
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * SUM(pi.qty),
     * null,
     * null,
     * null,
     * null,
     * null,
     * ph.poStatus
     * )
     * FROM PoHeader ph
     * JOIN ph.items pi
     * GROUP BY
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * ph.poStatus
     * """)
     * List<PoInspection1stLevelStatusDto> fetchPoInspectionStatus();
     */
    @Query("""
            SELECT new com.sarthi.dto.reports.PoInspection1stLevelStatusDto(
                0,
                ph.rlyShortName,
                ph.poNo,
                ph.poDate,
                ph.vendorDetails,

                COALESCE(ie.rio, ph.inspectingAgency),

                SUM(pi.qty),
                null,
                null,
                null,
                null,
                null,
                ph.poStatus
            )
            FROM PoHeader ph
            JOIN ph.items pi

            LEFT JOIN PincodePoIMapping ppm
                ON ppm.vendorCode = ph.vendorCode

            LEFT JOIN IEFieldsMapping ie
                ON ie.pinCode = ppm.pinCode

            GROUP BY
                ph.rlyShortName,
                ph.poNo,
                ph.poDate,
                ph.vendorDetails,
                COALESCE(ie.rio, ph.inspectingAgency),
                ph.poStatus
            """)
    List<PoInspection1stLevelStatusDto> fetchPoInspectionStatus();

    @Query("SELECT COUNT(ph.poNo) FROM PoHeader ph")
    long countTotalPo();
}
