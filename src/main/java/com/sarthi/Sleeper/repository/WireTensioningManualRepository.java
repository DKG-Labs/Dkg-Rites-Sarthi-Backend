package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.WireTensioning.WireTensioningManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WireTensioningManualRepository extends JpaRepository<WireTensioningManual, Long> {
}
