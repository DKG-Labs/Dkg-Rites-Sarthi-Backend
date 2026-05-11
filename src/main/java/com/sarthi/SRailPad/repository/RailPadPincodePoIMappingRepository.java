package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailPadPincodePoIMappingRepository extends JpaRepository<RailPadPincodePoIMapping, Long> {
    @Query("""
select r.vendorCode
from RailPadPincodePoIMapping r
where r.poiCode = :poiCode
""")
    Optional<String> findVendorCodeByPoiCode(
            @Param("poiCode") String poiCode
    );

    Optional<RailPadPincodePoIMapping> findByVendorCode(String vendorCode);

  //  Optional<RailPadPincodePoIMapping> findByPoiCodeAndPlantId(String poiCode, String plantId);
}
