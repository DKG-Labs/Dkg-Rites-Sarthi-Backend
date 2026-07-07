package com.sarthi.repository;

import com.sarthi.entity.CricsPos.AmendedPoHeader;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmendmentPoHeaderRepository extends JpaRepository<AmendedPoHeader, Long> {


    boolean existsByPoKey(String poKey);


}
