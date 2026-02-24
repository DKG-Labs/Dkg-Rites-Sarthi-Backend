package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BatchWeighment.BatchDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchDetailsRepository extends JpaRepository<BatchDetails, Long> {
}
