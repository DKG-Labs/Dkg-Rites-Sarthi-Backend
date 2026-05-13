package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.RlyProjection;
import com.sarthi.Sleeper.entity.RailwayMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailwayMasterRepository extends JpaRepository<RailwayMaster, Long> {
    @Query(value = """
            SELECT 
                rm.rly_code AS rlyCd,
                rm.rly_short_name AS rlyShortName
            FROM railway_master rm
            ORDER BY rm.rly_code
            """, nativeQuery = true)
    List<RlyProjection> getUniqueRlyList();
}
