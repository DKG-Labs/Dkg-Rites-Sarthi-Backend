package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProcessIcSaveChangesRepository extends JpaRepository<ProcessIcSaveChanges, Long> {
    Optional<ProcessIcSaveChanges> findByIcNumber(String icNumber);
}
