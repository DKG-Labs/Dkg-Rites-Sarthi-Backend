package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface RailProductionDeclarationRepository extends JpaRepository<RailProductionDeclaration, Long> {
    List<RailProductionDeclaration> findByPlantIdOrderByProductionDateDesc(String plantId);

    @Query(value = """
        SELECT 
            d.po_no AS poNo,
            GROUP_CONCAT(DISTINCT p.product_type SEPARATOR ', ') AS productTypes
        FROM rail_production_declaration d
        JOIN rail_production_product p ON d.id = p.declaration_id
        WHERE d.production_date BETWEEN :startDate AND :endDate
        GROUP BY d.po_no
    """, nativeQuery = true)
    List<Object[]> findProductTypesGroupedByPo(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate);
}
