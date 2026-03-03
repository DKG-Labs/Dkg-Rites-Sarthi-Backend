package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementBatchDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CementBatchDetailsRepository extends JpaRepository<CementBatchDetails, Long> {
}
