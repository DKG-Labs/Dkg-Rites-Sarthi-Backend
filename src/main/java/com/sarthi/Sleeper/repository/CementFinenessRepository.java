package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementFinenessTest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CementFinenessRepository extends JpaRepository<CementFinenessTest, Long> {
    Optional<CementFinenessTest> findByRequestId(Long requestId);
}
