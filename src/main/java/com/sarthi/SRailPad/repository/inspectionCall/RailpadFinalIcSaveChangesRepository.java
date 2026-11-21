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
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RailpadFinalIcSaveChanges> findByIcNumber(@org.springframework.data.repository.query.Param("icNumber") String icNumber);

    /** Move drafts saved under an old IC number to the new one (IC number follows the reassigned IE). */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE RailpadFinalIcSaveChanges e SET e.icNumber = :newNo WHERE e.icNumber = :oldNo")
    int renameIcNumber(@org.springframework.data.repository.query.Param("oldNo") String oldNo,
                       @org.springframework.data.repository.query.Param("newNo") String newNo);
}
