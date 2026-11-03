package com.sarthi.SRailPad.repository.scrap;

import com.sarthi.SRailPad.entity.scrap.RailScrapVerificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailScrapVerificationAuditRepository extends JpaRepository<RailScrapVerificationAudit, Long> {

    List<RailScrapVerificationAudit> findByScrapVerificationIdOrderByActionAtDesc(Long scrapVerificationId);
}
