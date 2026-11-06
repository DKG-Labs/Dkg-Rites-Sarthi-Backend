package com.sarthi.Sleeper.repository.scrap;

import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SleeperScrapVerificationAuditRepository extends JpaRepository<SleeperScrapVerificationAudit, Long> {

    List<SleeperScrapVerificationAudit> findByScrapVerificationIdOrderByActionAtDesc(Long scrapVerificationId);
}
