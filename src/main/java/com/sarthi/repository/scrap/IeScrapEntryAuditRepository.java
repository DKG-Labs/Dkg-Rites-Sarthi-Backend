package com.sarthi.repository.scrap;

import com.sarthi.entity.scrap.IeScrapEntryAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IeScrapEntryAuditRepository extends JpaRepository<IeScrapEntryAudit, Integer> {

    List<IeScrapEntryAudit> findByScrapEntryIdOrderByActionAtDesc(Integer scrapEntryId);
}
