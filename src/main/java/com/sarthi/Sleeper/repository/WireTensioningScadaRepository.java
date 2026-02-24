package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.WireTensioning.WireTensioningScada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WireTensioningScadaRepository extends JpaRepository<WireTensioningScada, Long> {
}
