package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailSheetingSizing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailSheetingSizingRepository extends JpaRepository<RailSheetingSizing, Long> {

    List<RailSheetingSizing> findByPlantIdAndVendorCodeOrderByCreatedDateDesc(String plantId, String vendorCode);

    Optional<RailSheetingSizing> findByBatchNoAndPlantIdAndVendorCode(String batchNo, String plantId, String vendorCode);

    /**
     * Ids of checks of the same batch on the same date and shift (plant with or without the leading ':').
     * A list, never a single result, so existing duplicates cannot break a save. excludeId: row being edited.
     */
    @org.springframework.data.jpa.repository.Query("""
        SELECT r.id FROM RailSheetingSizing r
        WHERE r.plantId IN :plantIds AND r.vendorCode = :vendorCode
          AND UPPER(TRIM(r.batchNo)) = :batchNo
          AND COALESCE(r.castingDate, '') = :castingDate
          AND UPPER(TRIM(COALESCE(r.shift, ''))) = :shift
          AND (:excludeId IS NULL OR r.id <> :excludeId)
        """)
    List<Long> findSameBatchDateShift(@org.springframework.data.repository.query.Param("plantIds") java.util.Collection<String> plantIds,
                                      @org.springframework.data.repository.query.Param("vendorCode") String vendorCode,
                                      @org.springframework.data.repository.query.Param("batchNo") String batchNo,
                                      @org.springframework.data.repository.query.Param("castingDate") String castingDate,
                                      @org.springframework.data.repository.query.Param("shift") String shift,
                                      @org.springframework.data.repository.query.Param("excludeId") Long excludeId);
}
