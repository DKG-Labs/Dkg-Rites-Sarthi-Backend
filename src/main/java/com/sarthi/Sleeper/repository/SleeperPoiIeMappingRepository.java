package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperPoiIeMappingRepository extends JpaRepository<SleeperPoiIeMapping, Long> {
    boolean existsByPoiCodeAndIeUserId(String poiCode, int intExact);

    @Query("SELECT p FROM SleeperPoiIeMapping p WHERE p.poiCode = :poiCode")
    List<SleeperPoiIeMapping> findByPoiCode(String poiCode);

    List<SleeperPoiIeMapping> findByPoiCodeAndIeType(String poiCode, String mainIe);

    boolean existsByPoiCodeAndIeUserIdAndIeType(String poiCode, int intExact, String mainIe);

    List<SleeperPoiIeMapping> findByIeUserId(Integer ieUserId);


    List<SleeperPoiIeMapping> findByPoiCodeAndPlantId(String poiCode, String plantId);


    List<SleeperPoiIeMapping> findByPoiCodeAndPlantIdAndIeType(String poiCode, String plantId, String ieType);


    boolean existsByPoiCodeAndPlantIdAndIeUserId(String poiCode, String plantId, Integer userId);

    boolean existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
            String poiCode,
            String plantId,
            Integer userId,
            String ieType
    );


    @Query("""
            SELECT m
            FROM SleeperPoiIeMapping m
            JOIN SleeperPincodePoIMapping s
            ON s.poiCode = m.poiCode
            WHERE s.companyName = :companyName
            AND m.plantId = :plantId
            AND m.ieType = :ieType
            """)
    List<SleeperPoiIeMapping> findMappedEmployees(
            String companyName,
            String plantId,
            String ieType
    );

}