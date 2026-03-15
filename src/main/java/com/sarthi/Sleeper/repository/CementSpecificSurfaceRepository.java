package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementSpecificSurface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CementSpecificSurfaceRepository extends JpaRepository<CementSpecificSurface, Long> {
}
