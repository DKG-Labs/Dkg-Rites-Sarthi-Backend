package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RailInspectionLotRepository extends JpaRepository<RailInspectionLot, Long> {

    @Query(value = """
        SELECT ril.id AS lotId, ril.lot_no AS lotNo, ric.call_no AS callNo
        FROM rail_inspection_lot ril
        JOIN rail_inspection_call ric ON ril.call_id = ric.id
        WHERE ric.plant_id = :plantId
          AND (YEAR(ric.created_at) = :year OR YEAR(ric.inspection_date) = :year)
        ORDER BY ril.lot_no, ric.call_no
    """, nativeQuery = true)
    List<Object[]> findLotsByPlantAndYear(@Param("plantId") String plantId, @Param("year") int year);

    @Query(value = "SELECT batch_no, production_date, quantity FROM rail_inspection_batch WHERE lot_id = :lotId", nativeQuery = true)
    List<Object[]> findBatchesByLotId(@Param("lotId") Long lotId);

    @Query(value = """
        SELECT COALESCE(SUM(pb.quantity), 0), MAX(pd.production_date)
        FROM rail_production_batch pb
        JOIN rail_production_product pp ON pb.product_id = pp.id
        JOIN rail_production_declaration pd ON pp.declaration_id = pd.id
        WHERE pd.plant_id = :plantId AND pb.batch_no = :batchNo
    """, nativeQuery = true)
    List<Object[]> findProductionByPlantAndBatch(@Param("plantId") String plantId, @Param("batchNo") String batchNo);

    @Query(value = """
        SELECT COALESCE(SUM(pr.rejected_qty), 0), MAX(v.casting_date)
        FROM rail_ie_production_rejection pr
        JOIN rail_ie_production_verification v ON pr.verification_id = v.id
        WHERE v.production_unit = :plantId AND pr.batch_no = :batchNo
    """, nativeQuery = true)
    List<Object[]> findProcessRejectionByPlantAndBatch(@Param("plantId") String plantId, @Param("batchNo") String batchNo);
}
