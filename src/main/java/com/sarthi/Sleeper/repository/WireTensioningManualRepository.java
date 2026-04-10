package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.WireTensioning.WireTensioningManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WireTensioningManualRepository extends JpaRepository<WireTensioningManual, Long> {
    @Query("""
    SELECT m FROM WireTensioningManual m
    WHERE m.wireTensioning.id IN :ids
""")
    List<WireTensioningManual> findByWireIds(List<Long> ids);
}
