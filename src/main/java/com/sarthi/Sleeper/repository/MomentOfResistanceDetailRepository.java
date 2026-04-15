package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MomentOfResistanceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MomentOfResistanceDetailRepository extends JpaRepository<MomentOfResistanceDetail, Long> {
}
