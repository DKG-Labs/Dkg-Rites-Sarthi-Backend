package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCubeT.SteamCubeTestingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SteamCubeTestingDetailsRepository extends JpaRepository<SteamCubeTestingDetails, Long> {
    @Query("""
    SELECT d FROM SteamCubeTestingDetails d
    WHERE d.steamCubeTesting.id IN :ids
""")
    List<SteamCubeTestingDetails> findBySteamIds(List<Long> ids);
}
