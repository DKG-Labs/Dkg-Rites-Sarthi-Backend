package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.CompanyProjection;
import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperPincodePoIMappingRepository extends JpaRepository<SleeperPincodePoIMapping, Long> {
    SleeperPincodePoIMapping findByVendorCode(String s);

    List<SleeperPincodePoIMapping> findAllByVendorCode(String vendorCode);

    Optional<SleeperPincodePoIMapping> findByCompanyNameAndUnitName(String companyName, String unitName);

    Optional<SleeperPincodePoIMapping> findByPoiCode(String poiCode);

    @Query("SELECT s.vendorCode FROM SleeperPincodePoIMapping s WHERE s.poiCode = :poiCode")
    Optional<String> findVendorCodeByPoiCode(@Param("poiCode") String poiCode);

    List<SleeperPincodePoIMapping> findByPoiCodeIn(List<String> poiCodes);

    @Query(value = """
    SELECT company_name AS companyName,
           vendor_code AS vendorCode
    FROM sleeper_pincode_poi_mapping
""", nativeQuery = true)
    List<CompanyProjection> getCompanies();

    @Query(value = "SELECT poi_code FROM sleeper_pincode_poi_mapping WHERE poi_code LIKE 'POI%' ORDER BY CAST(SUBSTRING(poi_code, 4) AS UNSIGNED) DESC LIMIT 1", nativeQuery = true)
    String findMaxNumericPoiCode();

    @Query("""
    SELECT DISTINCT s
    FROM SleeperPincodePoIMapping s
    ORDER BY s.companyName
    """)
    List<SleeperPincodePoIMapping> findAllCompanies();
}
