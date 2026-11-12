package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.HeatDtlSms3Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeatDtlSms3Repository extends JpaRepository<HeatDtlSms3Entity, String> {
        // @Query("SELECT h FROM HeatDtlSms3Entity h LEFT JOIN BloomDtlSms3Entity b ON
        // h.heatNo = b.castNo WHERE b.castNo IS NULL ORDER BY h.createdAt DESC")
        // List<HeatDtlSms3Entity> findHeatDetailsNotInBloomDetails();

        @Query("""
                            SELECT h
                            FROM HeatDtlSms3Entity h
                            JOIN DutyHeatSms3Entity d ON h.heatNo = d.dutyHeatRelationId.heatNo
                            WHERE (h.heatStage <> 'Bloom Cutting'
                               OR (h.heatStage = 'Bloom Cutting' AND d.dutyHeatRelationId.dutyId = :dutyId))
                               AND (h.isDiverted = false OR d.dutyHeatRelationId.dutyId = :dutyId)
                            ORDER BY h.createdAt DESC
                        """)
        List<HeatDtlSms3Entity> findHeatDetailsNotInBloomDetails(
                        @Param("dutyId") String dutyId);

        /**
         * Shift Summary heats: every heat linked to this duty, plus unfinished (not 'Complete'),
         * non-diverted heats from earlier duties of the same organisation + SMS.
         */
        @Query("""
            SELECT h FROM HeatDtlSms3Entity h
            WHERE EXISTS (SELECT 1 FROM DutyHeatSms3Entity d
                          WHERE d.dutyHeatRelationId.heatNo = h.heatNo
                            AND d.dutyHeatRelationId.dutyId = :dutyId)
               OR ((h.heatStage IS NULL OR h.heatStage <> 'Complete')
                   AND h.isDiverted = false
                   AND EXISTS (SELECT 1 FROM DutyHeatSms3Entity d2, SmsDutyEntity s
                               WHERE d2.dutyHeatRelationId.heatNo = h.heatNo
                                 AND s.dutyId = d2.dutyHeatRelationId.dutyId
                                 AND s.sms = :sms
                                 AND s.organisation = :organisation))
            ORDER BY h.createdAt DESC
            """)
        List<HeatDtlSms3Entity> findShiftSummaryHeats(@Param("dutyId") String dutyId,
                                                     @Param("sms") String sms,
                                                     @Param("organisation") String organisation);

        Optional<HeatDtlSms3Entity> findByHeatNo(String heatNo);
}
