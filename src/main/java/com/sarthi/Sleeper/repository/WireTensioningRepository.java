package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.WireTensioning.WireTensioning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WireTensioningRepository extends JpaRepository<WireTensioning, Long> {
}
