package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessIcEditRepository extends JpaRepository<ProcessIcEdit, Long> {
    Optional<ProcessIcEdit> findByIcNumber(String icNumber);
}
