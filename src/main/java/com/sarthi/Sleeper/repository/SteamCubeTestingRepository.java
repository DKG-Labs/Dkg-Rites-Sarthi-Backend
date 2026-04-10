package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCubeT.SteamCubeTesting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SteamCubeTestingRepository extends JpaRepository<SteamCubeTesting, Long> {

    @Query("""
    SELECT s FROM SteamCubeTesting s
    WHERE s.plantId = :plantId
    AND s.vendorCode = :vendorCode
    AND s.shift = :shift
    AND s.createdBy = :createdBy
    AND s.createdDate BETWEEN :start AND :end
""")
    List<SteamCubeTesting> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime start,
            LocalDateTime end
    );
}
