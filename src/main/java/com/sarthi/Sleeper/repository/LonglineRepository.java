package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.LonglineMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LonglineRepository extends JpaRepository<LonglineMaster, Long> {
}