package com.sarthi.repository;

import com.sarthi.entity.IBS.SarthiIbsPoiMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SarthiIbsPoiMappingRepository extends JpaRepository<SarthiIbsPoiMapping, Long> {

    Optional<SarthiIbsPoiMapping> findByPoiCodeAndProductType(String poiCode, String productType);

    Optional<SarthiIbsPoiMapping> findByPoiCode(String poiCode);

    @Query("SELECT s FROM SarthiIbsPoiMapping s WHERE (LOWER(s.poiCode) = LOWER(:poiCode) OR LOWER(s.poiCode) = LOWER(CONCAT(':', :poiCode)) OR LOWER(REPLACE(s.poiCode, ':', '')) = LOWER(REPLACE(:poiCode, ':', ''))) AND (:productType IS NULL OR LOWER(s.productType) = LOWER(:productType))")
    List<SarthiIbsPoiMapping> findFlexibleMatch(@Param("poiCode") String poiCode, @Param("productType") String productType);

    default Optional<SarthiIbsPoiMapping> findBestMatch(String poiCode, String productType) {
        if (poiCode == null || poiCode.trim().isEmpty()) return Optional.empty();
        List<SarthiIbsPoiMapping> list = findFlexibleMatch(poiCode.trim(), productType);
        return list != null && !list.isEmpty() ? Optional.of(list.get(0)) : Optional.empty();
    }
}

