package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.ApprovedQAP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RailApprovedQAPRepository extends JpaRepository<ApprovedQAP, Long> {
    List<ApprovedQAP> findAllByVendorCode(String vendorCode);
    List<ApprovedQAP> findAllByPlantId(String plantId);

    @Query(value = """
        SELECT * FROM rail_approved_qap q
        WHERE (
            :vendorCode IS NOT NULL AND :vendorCode <> '' AND (
                REPLACE(TRIM(COALESCE(q.vendor_code, '')), ':', '') = REPLACE(TRIM(:vendorCode), ':', '')
                OR :vendorCode LIKE CONCAT('%', REPLACE(TRIM(COALESCE(q.vendor_code, '')), ':', ''), '%')
            )
        )
        OR (
            :plantId IS NOT NULL AND :plantId <> '' AND (
                REPLACE(TRIM(COALESCE(q.plant_id, '')), ':', '') = REPLACE(TRIM(:plantId), ':', '')
                OR :plantId LIKE CONCAT('%', REPLACE(TRIM(COALESCE(q.plant_id, '')), ':', ''), '%')
                OR REPLACE(TRIM(COALESCE(q.plant_id, '')), ':', '') LIKE CONCAT('%', SUBSTRING_INDEX(TRIM(:plantId), '/', -1), '%')
            )
        )
        OR (
            :vendorName IS NOT NULL AND :vendorName <> '' AND (
                UPPER(COALESCE(q.vendor_name, '')) LIKE CONCAT('%', UPPER(:vendorName), '%')
                OR UPPER(:vendorName) LIKE CONCAT('%', UPPER(COALESCE(q.vendor_name, '')), '%')
            )
        )
        ORDER BY q.id DESC
    """, nativeQuery = true)
    List<ApprovedQAP> findApprovedQapByAnyMatch(@Param("vendorCode") String vendorCode, 
                                               @Param("plantId") String plantId, 
                                               @Param("vendorName") String vendorName);
}
