package com.sarthi.SRailPad.diversion.repository;

import com.sarthi.SRailPad.diversion.entity.RailDiversionCallAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailDiversionCallAllocationRepository extends JpaRepository<RailDiversionCallAllocation, Long> {

    List<RailDiversionCallAllocation> findByCallNoAndStatus(String callNo, String status);

    List<RailDiversionCallAllocation> findByCallNo(String callNo);
}
