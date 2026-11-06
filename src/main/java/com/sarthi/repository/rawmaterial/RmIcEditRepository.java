package com.sarthi.repository.rawmaterial;

import com.sarthi.entity.rawmaterial.RmIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RmIcEditRepository extends JpaRepository<RmIcEdit, Long> {
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT * FROM rm_ic_edit
            WHERE ic_number = :icNumber
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RmIcEdit> findByIcNumber(@org.springframework.data.repository.query.Param("icNumber") String icNumber);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT * FROM rm_ic_edit
            WHERE ic_number = :icNumber
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RmIcEdit> findFirstByIcNumber(@org.springframework.data.repository.query.Param("icNumber") String icNumber);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT * FROM rm_ic_edit
            WHERE ic_number = :icNumber
               OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = :icNumber
               OR ic_number LIKE CONCAT('%/', :icNumber, '/%')
               OR ic_number LIKE CONCAT('%', :icNumber, '%')
               OR :icNumber LIKE CONCAT('%', ic_number, '%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RmIcEdit> findFirstByIcNumberOrderByIdDesc(@org.springframework.data.repository.query.Param("icNumber") String icNumber);

    @Modifying
    @Query(value = """
            UPDATE rm_ic_edit
            SET created_at = :newDateTime
            WHERE ic_number = :callNo OR ic_number LIKE CONCAT('%/', :callNo, '/%')
            """, nativeQuery = true)
    int updateCreatedAtByCallNo(
            @Param("callNo") String callNo,
            @Param("newDateTime") LocalDateTime newDateTime);

    @Query(value = """
            SELECT ic_number, created_at FROM rm_ic_edit
            WHERE ic_number = :callNo OR ic_number LIKE CONCAT('%/', :callNo, '/%')
            ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    java.util.List<Object[]> findIcNumberAndCreatedAtByCallNo(@Param("callNo") String callNo);
}
