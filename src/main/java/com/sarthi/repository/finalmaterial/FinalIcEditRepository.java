package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinalIcEditRepository extends JpaRepository<FinalIcEdit, Long> {
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT * FROM final_ic_edit
            WHERE ic_number = :icNumber
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<FinalIcEdit> findByIcNumber(@org.springframework.data.repository.query.Param("icNumber") String icNumber);
}
