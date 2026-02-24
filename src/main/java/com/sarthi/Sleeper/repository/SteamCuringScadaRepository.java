package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCuring.SteamCuringScada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SteamCuringScadaRepository extends JpaRepository<SteamCuringScada, Long> {
}
