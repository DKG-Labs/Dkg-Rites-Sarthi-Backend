package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.HtsWirePlacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HtsWirePlacementRepository extends JpaRepository<HtsWirePlacement, Long> {


    @Query("""
    SELECT h FROM HtsWirePlacement h
    WHERE h.plantId = :plantId
    AND h.vendorCode = :vendorCode
    AND h.shift = :shift
    AND h.createdBy = :createdBy
    AND h.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<HtsWirePlacement> findTodayData(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );


    @Query("""
    SELECT h FROM HtsWirePlacement h
    WHERE h.plantId = :plantId
    AND h.vendorCode = :vendorCode
    AND h.shift = :shift
    AND h.createdBy = :createdBy
    AND h.createdDate BETWEEN :startOfDay AND :endOfDay
    AND h.status = 'A'
""")
    List<HtsWirePlacement> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

}
