package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCubeSampleDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SteamCubeSampleDeclarationRepository extends JpaRepository<SteamCubeSampleDeclaration, Long> {
}
