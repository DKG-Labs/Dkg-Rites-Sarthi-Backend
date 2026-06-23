package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.HeatDtlSms2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeatDtlSms2Repository extends JpaRepository<HeatDtlSms2Entity, String> {
        // @Query("SELECT h FROM HeatDtlSms2Entity h LEFT JOIN BloomDtlSms2Entity b ON h.heatNo = b.castNo WHERE b.castNo IS NULL ORDER BY h.createdAt DESC")
        // List<HeatDtlSms2Entity> findHeatDetailsNotInBloomDetails();
        @Query("""
    SELECT h
    FROM HeatDtlSms2Entity h
    JOIN DutyHeatSms2Entity d ON h.heatNo = d.dutyHeatRelationId.heatNo
    WHERE (h.heatStage <> 'Bloom Cutting'
       OR (h.heatStage = 'Bloom Cutting' AND d.dutyHeatRelationId.dutyId = :dutyId))
       AND (h.isDiverted = false OR d.dutyHeatRelationId.dutyId = :dutyId)
    ORDER BY h.createdAt DESC
""")
List<HeatDtlSms2Entity> findHeatDetailsNotInBloomDetails(
    @Param("dutyId") String dutyId
);

        Optional<HeatDtlSms2Entity> findByHeatNo(String heatNo);
}
