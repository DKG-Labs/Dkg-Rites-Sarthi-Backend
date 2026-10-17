package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessIcEditRepository extends JpaRepository<ProcessIcEdit, Long> {
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT * FROM process_ic_edit
            WHERE ic_number = :icNumber
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<ProcessIcEdit> findByIcNumber(@org.springframework.data.repository.query.Param("icNumber") String icNumber);
}
