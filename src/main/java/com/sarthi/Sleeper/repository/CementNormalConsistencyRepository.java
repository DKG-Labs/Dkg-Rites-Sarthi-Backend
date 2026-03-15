package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementNormalConsistency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CementNormalConsistencyRepository extends JpaRepository<CementNormalConsistency, Long> {
}
