package com.sarthi.repository;

import com.sarthi.entity.CricsPos.PoMaHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoMaHeaderRepository extends JpaRepository<PoMaHeader, Long> {
    boolean existsByMaKey(String maKey);

    List<PoMaHeader> findByPoNo(String poNo);

    List<PoMaHeader> findByPoKey(String poKey);
}
