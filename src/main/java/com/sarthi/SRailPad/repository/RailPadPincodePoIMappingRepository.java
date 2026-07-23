package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    Optional<RailPadPincodePoIMapping> findByVendorCodeAndCompanyName(String vendorCode, String companyName);

    List<RailPadPincodePoIMapping> findByPoiCode(String poiCode);

    @Query("SELECT DISTINCT r.companyName FROM RailPadPincodePoIMapping r WHERE r.poiCode IN :poiCodes AND r.companyName IS NOT NULL")
    List<String> findDistinctCompanyNamesByPoiCodeIn(@Param("poiCodes") List<String> poiCodes);

    List<RailPadPincodePoIMapping> findByCompanyName(String companyName);

    @Query("SELECT DISTINCT r.vendorCode, r.companyName, r.poiCode FROM RailPadPincodePoIMapping r WHERE r.vendorCode IS NOT NULL ORDER BY r.companyName")
    List<Object[]> findDistinctManufacturers();

  //  Optional<RailPadPincodePoIMapping> findByPoiCodeAndPlantId(String poiCode, String plantId);
}
