package com.sarthi.repository;

import com.sarthi.entity.UdmSyncLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UdmSyncLogRepository extends JpaRepository<UdmSyncLog, Long> {

    /** Latest runs first */
    List<UdmSyncLog> findAllByOrderByStartedAtDesc(Pageable pageable);
}
