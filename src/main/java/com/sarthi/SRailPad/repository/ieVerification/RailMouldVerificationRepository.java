package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailMouldVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RailMouldVerificationRepository extends JpaRepository<RailMouldVerification, Long> {
    List<RailMouldVerification> findByPlantIdAndVendorCodeOrderByCreatedDateDesc(String plantId, String vendorCode);

    /**
     * Ids of existing checks of the same mould on the same date and shift (plant with or without the leading ':').
     * Returns a list, never a single result, so legacy duplicate rows can't break a save.
     * excludeId: the row being edited (null on create).
     */
    @Query("""
        SELECT m.id FROM RailMouldVerification m
        WHERE m.plantId IN :plantIds
          AND m.vendorCode = :vendorCode
          AND UPPER(TRIM(m.mouldNumber)) = :mouldNumber
          AND COALESCE(m.castingDate, '') = :castingDate
          AND UPPER(TRIM(COALESCE(m.shift, ''))) = :shift
          AND (:excludeId IS NULL OR m.id <> :excludeId)
        """)
    List<Long> findSameMouldDateShift(@Param("plantIds") Collection<String> plantIds,
                                      @Param("vendorCode") String vendorCode,
                                      @Param("mouldNumber") String mouldNumber,
                                      @Param("castingDate") String castingDate,
                                      @Param("shift") String shift,
                                      @Param("excludeId") Long excludeId);
}
