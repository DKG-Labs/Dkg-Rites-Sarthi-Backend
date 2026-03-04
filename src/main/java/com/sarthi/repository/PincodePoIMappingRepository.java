package com.sarthi.repository;

import com.sarthi.dto.UnitDetailsDTO;
import com.sarthi.dto.UnitDto;
import com.sarthi.entity.PincodePoIMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PincodePoIMappingRepository extends JpaRepository<PincodePoIMapping, Long> {
    boolean existsByPinCodeAndPoiCode(String pinCode, String poiCode);

    Optional<PincodePoIMapping> findByPoiCode(String placeOfInspection);

    // @Query("SELECT DISTINCT p.companyName FROM PincodePoIMapping p")
    // List<String> findDistinctCompanyNames();

    // @Query("""
    // SELECT DISTINCT p.companyName
    // FROM PincodePoIMapping p
    // WHERE p.poiCode IS NOT NULL
    // AND p.poiCode <> ''
    // """)
    // List<String> findDistinctCompanyNames();

    @Query("""
                SELECT DISTINCT p.companyName
                FROM PincodePoIMapping p
                WHERE p.poiCode IN ('POI1', 'POI31','POI32','POI33')
            """)
    List<String> findDistinctCompanyNames();

    @Query("""
                SELECT DISTINCT p.companyName
                FROM PincodePoIMapping p
                WHERE p.vendorCode = :vendorCode
            """)
    List<String> findDistinctCompanyNamesByVendorCode(
            @Param("vendorCode") String vendorCode);

    @Query("""
                SELECT DISTINCT new com.sarthi.dto.UnitDto(p.unitName)
                FROM PincodePoIMapping p
                WHERE p.companyName = :companyName
            """)
    List<UnitDto> findUnitsByCompany(@Param("companyName") String companyName);

    @Query("""
                SELECT new com.sarthi.dto.UnitDetailsDTO(
                    p.address,
                    p.poiCode,
                    p.pinCode
                )
                FROM PincodePoIMapping p
                WHERE p.companyName = :companyName
                  AND p.unitName = :unitName
            """)
    Optional<UnitDetailsDTO> findUnitDetails(
            @Param("companyName") String companyName,
            @Param("unitName") String unitName);

    @Query("SELECT DISTINCT p.companyName FROM PincodePoIMapping p WHERE p.poiCode IS NOT NULL AND p.poiCode <> ''")
    List<String> findAllDistinctCompanyNames();

    @Query("SELECT DISTINCT p.unitName FROM PincodePoIMapping p WHERE p.companyName = :companyName AND p.poiCode IS NOT NULL AND p.poiCode <> ''")
    List<String> findUnitNamesByCompanyName(@Param("companyName") String companyName);

    Optional<PincodePoIMapping> findByCompanyNameAndUnitName(String companyName, String unitName);

}
