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

    @Query("SELECT d FROM RailProductionDeclaration d WHERE d.productionDate = :date AND d.shift = :shift " +
           "AND (:productionLine IS NULL OR :productionLine = '' OR d.productionLine = :productionLine OR TRIM(d.productionLine) = TRIM(:productionLine) OR REPLACE(d.productionLine, ' ', '') = REPLACE(:productionLine, ' ', '')) " +
           "AND (:poNo IS NULL OR :poNo = '' OR d.poNo = :poNo OR d.poNo LIKE CONCAT('%', :poNo, '%') OR :poNo LIKE CONCAT('%', d.poNo, '%'))")
    List<RailProductionDeclaration> findByShiftDetails(
        @Param("date") java.time.LocalDate date,
        @Param("shift") String shift,
        @Param("productionLine") String productionLine,
        @Param("poNo") String poNo);

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

    @Query(value = """
        SELECT 
            d.po_no AS poNo,
            GROUP_CONCAT(DISTINCT p.product_type SEPARATOR ', ') AS productTypes
        FROM rail_production_declaration d
        JOIN rail_production_product p ON d.id = p.declaration_id
        WHERE d.po_no IS NOT NULL AND d.po_no <> ''
          AND p.product_type IS NOT NULL AND p.product_type <> ''
        GROUP BY d.po_no
    """, nativeQuery = true)
    List<Object[]> findDistinctProductTypesGroupByPo();
}
