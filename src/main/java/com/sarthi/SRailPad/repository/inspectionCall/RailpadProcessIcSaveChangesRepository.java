package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadProcessIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RailpadProcessIcSaveChangesRepository extends JpaRepository<RailpadProcessIcSaveChanges, Long> {
    @Query(value = """
            SELECT * FROM railpad_process_ic_save_changes
            WHERE ic_number = :icNumber
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RailpadProcessIcSaveChanges> findByIcNumber(@org.springframework.data.repository.query.Param("icNumber") String icNumber);

    /** Move drafts saved under an old IC number to the new one (IC number follows the reassigned IE). */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE RailpadProcessIcSaveChanges e SET e.icNumber = :newNo WHERE e.icNumber = :oldNo")
    int renameIcNumber(@org.springframework.data.repository.query.Param("oldNo") String oldNo,
                       @org.springframework.data.repository.query.Param("newNo") String newNo);
}
