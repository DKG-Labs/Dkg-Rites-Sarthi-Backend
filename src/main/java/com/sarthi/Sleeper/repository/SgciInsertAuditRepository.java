package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SgciInsertAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SgciInsertAuditRepository extends JpaRepository<SgciInsertAudit, Long> {
    List<SgciInsertAudit> findByConsignmentNo(String consignmentNo);
    Optional<SgciInsertAudit> findByRequestId(Long requestId);
}
