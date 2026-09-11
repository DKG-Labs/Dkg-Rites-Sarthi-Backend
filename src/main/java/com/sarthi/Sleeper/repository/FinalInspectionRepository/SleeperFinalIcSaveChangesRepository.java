package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperFinalIcSaveChangesRepository extends JpaRepository<SleeperFinalIcSaveChanges, Long> {

    @Query("SELECT s FROM SleeperFinalIcSaveChanges s WHERE LOWER(TRIM(s.icNumber)) = LOWER(TRIM(:icNumber)) ORDER BY s.id DESC")
    List<SleeperFinalIcSaveChanges> findAllByIcNumberFlexible(@Param("icNumber") String icNumber);

    default Optional<SleeperFinalIcSaveChanges> findByIcNumber(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        List<SleeperFinalIcSaveChanges> list = findAllByIcNumberFlexible(icNumber);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
