package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Final Inspection Details
 */
@Repository
public interface FinalInspectionDetailsRepository extends JpaRepository<FinalInspectionDetails, Long> {

    /**
     * Find Final Inspection Details by Inspection Call ID
     */
    @Query("SELECT fd FROM FinalInspectionDetails fd WHERE fd.inspectionCall.id = :icId")
    Optional<FinalInspectionDetails> findByIcId(@Param("icId") Long icId);

    /**
     * Find Final Inspection Details by RM IC Number
     */
    Optional<FinalInspectionDetails> findByRmIcNumber(String rmIcNumber);

    /**
     * Find Final Inspection Details by Process IC Number
     */
    Optional<FinalInspectionDetails> findByProcessIcNumber(String processIcNumber);

    @Query("SELECT fd FROM FinalInspectionDetails fd JOIN FETCH fd.inspectionCall WHERE fd.inspectionCall.icNumber = :icNumber")
    Optional<FinalInspectionDetails> findByIcNumberWithCall(@Param("icNumber") String icNumber);


    /**
     * Sum totalOfferedQty for all inspection calls with the same PO Serial No that occurred before the given ID.
     */
    @Query("SELECT SUM(fd.totalOfferedQty) FROM FinalInspectionDetails fd WHERE fd.inspectionCall.poSerialNo = :poSerialNo AND fd.id < :currentId")
    Long sumOfferedQtyByPoSerialNoAndIdLessThan(@Param("poSerialNo") String poSerialNo, @Param("currentId") Long currentId);

    /**
     * Sum totalAcceptedQty for all inspection calls with the same PO Serial No that occurred before the given ID.
     */
    @Query("SELECT SUM(fd.totalAcceptedQty) FROM FinalInspectionDetails fd WHERE fd.inspectionCall.poSerialNo = :poSerialNo AND fd.id < :currentId")
    Long sumAcceptedQtyByPoSerialNoAndIdLessThan(@Param("poSerialNo") String poSerialNo, @Param("currentId") Long currentId);

}

