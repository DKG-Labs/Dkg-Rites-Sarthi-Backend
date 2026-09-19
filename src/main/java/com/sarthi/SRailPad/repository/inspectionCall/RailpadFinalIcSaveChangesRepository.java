package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadFinalIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
public interface RailpadFinalIcSaveChangesRepository extends JpaRepository<RailpadFinalIcSaveChanges, Long> {
    @Query(value = """
            SELECT * FROM railpad_final_ic_save_changes
            WHERE ic_number = :icNumber
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RailpadFinalIcSaveChanges> findByIcNumber(String icNumber);
}
