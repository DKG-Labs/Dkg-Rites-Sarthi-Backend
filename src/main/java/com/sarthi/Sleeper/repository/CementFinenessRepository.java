package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementFinenessTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CementFinenessRepository extends JpaRepository<CementFinenessTest, Long> {
}
