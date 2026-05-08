package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailVendorPlantsRepository extends JpaRepository<RailVendorPlants, Long> {
    @Query("""
select r.vendorId
from RailVendorPlants r
where r.vendorCode = :vendorCode
""")
    Optional<Long> findVendorUserIdByVendorCode(
            @Param("vendorCode") String vendorCode
    );
}
