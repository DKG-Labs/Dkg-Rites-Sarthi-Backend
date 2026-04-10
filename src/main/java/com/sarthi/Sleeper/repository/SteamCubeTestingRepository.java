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
    WHERE s.location = :location
    AND s.batchNo = :batchNo
    AND s.createdDate BETWEEN :start AND :end
""")
    List<SteamCubeTesting> findByDate(
            String location,
            String batchNo,
            LocalDateTime start,
            LocalDateTime end
    );
}
