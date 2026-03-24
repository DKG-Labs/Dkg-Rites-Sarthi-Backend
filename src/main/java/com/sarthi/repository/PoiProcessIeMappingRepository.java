package com.sarthi.repository;

import com.sarthi.entity.PoiProcessIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoiProcessIeMappingRepository extends JpaRepository<PoiProcessIeMapping, Long> {
    @Query("""
    SELECT u.id
    FROM PoiProcessIeMapping p
    JOIN UserMaster u 
        ON u.employeeCode = p.employeeCode
    WHERE p.poiCode = :poiCode
""")
    List<Long> findUserIdsByPoiCode(@Param("poiCode") String poiCode);
}
