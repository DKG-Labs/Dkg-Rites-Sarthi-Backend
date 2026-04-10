package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.WireTensioning.WireTensioningScada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WireTensioningScadaRepository extends JpaRepository<WireTensioningScada, Long> {
    @Query("""
    SELECT s FROM WireTensioningScada s
    WHERE s.wireTensioning.id IN :ids
""")
    List<WireTensioningScada> findByWireIds(List<Long> ids);
}
