package com.sarthi.repository.ercdiversion;

import com.sarthi.entity.ercdiversion.ErcDiversionCallAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErcDiversionCallAllocationRepository extends JpaRepository<ErcDiversionCallAllocation, Long> {

    List<ErcDiversionCallAllocation> findByCallId(Long callId);

    List<ErcDiversionCallAllocation> findByCallNo(String callNo);
}
