package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCuring.SteamCuring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SteamCuringRepository extends JpaRepository<SteamCuring, Long> {
    @Query("""
    SELECT s FROM SteamCuring s
    WHERE s.plantId = :plantId
    AND s.vendorCode = :vendorCode
    AND s.shift = :shift
    AND s.createdBy = :createdBy
    AND s.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<SteamCuring> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
