package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SleeperFinalIcSaveChangesRepository extends JpaRepository<SleeperFinalIcSaveChanges, Long> {

    @Query("SELECT s FROM SleeperFinalIcSaveChanges s WHERE LOWER(TRIM(s.icNumber)) = LOWER(TRIM(:icNumber)) ORDER BY s.id DESC")
    java.util.List<SleeperFinalIcSaveChanges> findAllByIcNumberFlexible(@org.springframework.data.repository.query.Param("icNumber") String icNumber);

    default Optional<SleeperFinalIcSaveChanges> findByIcNumber(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        java.util.List<SleeperFinalIcSaveChanges> list = findAllByIcNumberFlexible(icNumber);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
