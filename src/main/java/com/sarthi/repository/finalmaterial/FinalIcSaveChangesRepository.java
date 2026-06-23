package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FinalIcSaveChangesRepository extends JpaRepository<FinalIcSaveChanges, Long> {
    Optional<FinalIcSaveChanges> findByIcNumber(String icNumber);
}
