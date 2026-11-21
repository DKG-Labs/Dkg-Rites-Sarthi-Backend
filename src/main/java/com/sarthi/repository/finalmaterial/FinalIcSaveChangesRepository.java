package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FinalIcSaveChangesRepository extends JpaRepository<FinalIcSaveChanges, Long> {
    Optional<FinalIcSaveChanges> findByIcNumber(String icNumber);

    /** Move rows saved under an old IC number to the new one (IC number follows the reassigned IE). */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE FinalIcSaveChanges e SET e.icNumber = :newNo WHERE e.icNumber = :oldNo")
    int renameIcNumber(@org.springframework.data.repository.query.Param("oldNo") String oldNo,
                       @org.springframework.data.repository.query.Param("newNo") String newNo);
}
