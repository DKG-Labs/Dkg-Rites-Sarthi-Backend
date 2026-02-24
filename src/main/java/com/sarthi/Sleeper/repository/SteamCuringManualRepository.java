package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCuring.SteamCuringManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SteamCuringManualRepository extends JpaRepository<SteamCuringManual, Long> {
}
