package com.sarthi.repository.rawmaterial;

import com.sarthi.entity.rawmaterial.RmIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RmIcEditRepository extends JpaRepository<RmIcEdit, Long> {
    Optional<RmIcEdit> findByIcNumber(String icNumber);
    Optional<RmIcEdit> findFirstByIcNumber(String icNumber);
    Optional<RmIcEdit> findFirstByIcNumberOrderByIdDesc(String icNumber);
}
