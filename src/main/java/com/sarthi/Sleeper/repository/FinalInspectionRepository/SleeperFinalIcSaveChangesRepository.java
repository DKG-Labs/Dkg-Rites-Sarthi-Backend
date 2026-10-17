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

    @Query(value = """
            SELECT * FROM sleeper_final_ic_save_changes
            WHERE ic_number = :icNumber
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC
            """, nativeQuery = true)
    List<SleeperFinalIcSaveChanges> findAllByIcNumberFlexible(@Param("icNumber") String icNumber);

    default Optional<SleeperFinalIcSaveChanges> findByIcNumber(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        List<SleeperFinalIcSaveChanges> list = findAllByIcNumberFlexible(icNumber);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
