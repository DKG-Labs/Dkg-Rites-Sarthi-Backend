package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinalIcEditRepository extends JpaRepository<FinalIcEdit, Long> {
    Optional<FinalIcEdit> findByIcNumber(String icNumber);
}
