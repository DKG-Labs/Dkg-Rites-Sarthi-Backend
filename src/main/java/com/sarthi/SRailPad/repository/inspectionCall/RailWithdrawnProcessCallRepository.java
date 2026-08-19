package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailWithdrawnProcessCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailWithdrawnProcessCallRepository extends JpaRepository<RailWithdrawnProcessCall, Long> {
    Optional<RailWithdrawnProcessCall> findByCallNo(String callNo);
}
