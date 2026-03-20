package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementNormalConsistency;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CementNormalConsistencyRepository extends JpaRepository<CementNormalConsistency, Long> {
    Optional<CementNormalConsistency> findByRequestId(Long requestId);
}
