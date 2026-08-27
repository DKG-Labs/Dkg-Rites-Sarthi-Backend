package com.sarthi.repository;

import com.sarthi.entity.CricsPos.CrisSyncStatus;
import org.modelmapper.internal.bytebuddy.asm.Advice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrisSyncStatusRepository extends JpaRepository<CrisSyncStatus, Long> {

    boolean existsByRefTypeAndRefKey(String po, String pokey);

    java.util.Optional<CrisSyncStatus> findByRefTypeAndRefKey(String refType, String refKey);
}
