package com.sarthi.repository;

import com.sarthi.entity.IePoiMapping;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IePoiMappingRepository extends JpaRepository<IePoiMapping, Long> {
    List<IePoiMapping> findAllByPoiCode(String poiCode);

    boolean existsByIeUserIdAndPoiCode(Integer ieUserId, String poiCode);

    Optional<IePoiMapping>
    findTopByPoiCodeOrderByCreatedDateDesc(String poiCode);

    List<IePoiMapping> findByPoiCode(String poiCode);
}
