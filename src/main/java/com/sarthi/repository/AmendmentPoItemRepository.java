package com.sarthi.repository;

import com.sarthi.entity.CricsPos.AmendedPoHeader;
import com.sarthi.entity.CricsPos.AmendedPoItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmendmentPoItemRepository extends JpaRepository<AmendedPoItem, Long> {


    List<AmendedPoItem> findByAmendedPoHeader(AmendedPoHeader amendedHeader);
}
