package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCuring.SteamCuringScada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SteamCuringScadaRepository extends JpaRepository<SteamCuringScada, Long> {
    @Query("""
    SELECT s FROM SteamCuringScada s
    WHERE s.steamCuring.id IN :ids
""")
    List<SteamCuringScada> findBySteamIds(List<Long> ids);
}
