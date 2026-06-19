package com.sarthi.repository.rawmaterial;

import com.sarthi.entity.rawmaterial.RmIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RmIcSaveChangesRepository extends JpaRepository<RmIcSaveChanges, Long> {
    Optional<RmIcSaveChanges> findByIcNumber(String icNumber);
}
