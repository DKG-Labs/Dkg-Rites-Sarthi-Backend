package com.sarthi.repository;

import com.sarthi.entity.CricsPos.PoMaDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoMaDetailRepository extends JpaRepository<PoMaDetail, Long> {
    List<PoMaDetail> findByMaPoHeaderPoNo(String poNo);
    List<PoMaDetail> findByMaPoHeaderPoKey(String poKey);
}
