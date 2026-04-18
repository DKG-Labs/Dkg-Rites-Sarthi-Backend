package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementSpecificSurface;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CementSpecificSurfaceRepository extends JpaRepository<CementSpecificSurface, Long> {
    Optional<CementSpecificSurface> findByRequestId(Long requestId);
    List<CementSpecificSurface> findAllByTypeOfTesting(String typeOfTesting);
}
