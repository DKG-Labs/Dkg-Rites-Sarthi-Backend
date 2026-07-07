package com.sarthi.repository;

import com.sarthi.entity.RegionSbuHead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionSbuHeadRepository extends JpaRepository<RegionSbuHead, Integer> {

    Optional<RegionSbuHead> findByRegionName(String regionName);
    void deleteBySbuHeadUserId(Integer sbuHeadUserId);
}
