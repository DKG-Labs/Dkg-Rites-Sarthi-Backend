package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCuring.SteamCuringManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SteamCuringManualRepository extends JpaRepository<SteamCuringManual, Long> {
    @Query("""
    SELECT m FROM SteamCuringManual m
    WHERE m.steamCuring.id IN :ids
""")
    List<SteamCuringManual> findBySteamIds(List<Long> ids);
}
