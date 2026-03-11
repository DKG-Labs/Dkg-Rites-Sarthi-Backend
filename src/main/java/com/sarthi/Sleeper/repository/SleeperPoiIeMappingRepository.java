package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SleeperPoiIeMappingRepository extends JpaRepository<SleeperPoiIeMapping, Long> {
    boolean existsByPoiCodeAndIeUserId(String poiCode, int intExact);

    @Query("SELECT p FROM SleeperPoiIeMapping p WHERE p.poiCode = :poiCode")
    List<SleeperPoiIeMapping> findByPoiCode(String poiCode);

    List<SleeperPoiIeMapping> findByPoiCodeAndIeType(String poiCode, String mainIe);

    boolean existsByPoiCodeAndIeUserIdAndIeType(String poiCode, int intExact, String mainIe);
}
