package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.WireTensioning.WireTensioning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WireTensioningRepository extends JpaRepository<WireTensioning, Long> {

    @Query("""
    SELECT w FROM WireTensioning w
    WHERE w.plantId = :plantId
    AND w.vendorCode = :vendorCode
    AND w.shift = :shift
    AND w.createdBy = :createdBy
    AND w.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<WireTensioning> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );


}
