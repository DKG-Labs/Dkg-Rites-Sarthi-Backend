package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.SteamProjection;
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

    @Query(value = """
    SELECT sc.entry_date,
           AVG((scm.min_temp + scm.max_temp) / 2) AS avg_temp
    FROM steam_curing sc
    JOIN steam_curing_manual scm 
         ON sc.id = scm.steam_curing_id
    WHERE sc.batch_no = :batchNo
    GROUP BY sc.entry_date
""", nativeQuery = true)
    Object[] getSteamCuringData(String batchNo);

    @Query(value = """
    SELECT sc.entry_date AS entryDate,
           (AVG(scm.min_temp) + AVG(scm.max_temp)) / 2 AS avgTemp
    FROM steam_curing sc
    JOIN steam_curing_manual scm ON sc.id = scm.steam_curing_id
    WHERE sc.batch_no = :batchNo
    GROUP BY sc.entry_date
    LIMIT 1
""", nativeQuery = true)
    SteamProjection getSteamData(String batchNo);
}
