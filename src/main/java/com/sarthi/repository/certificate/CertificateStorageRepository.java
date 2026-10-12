package com.sarthi.repository.certificate;

import com.sarthi.entity.certificate.CertificateStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateStorageRepository extends JpaRepository<CertificateStorage, Long> {
    Optional<CertificateStorage> findByIcNumber(String icNumber);

    @Query("""
       SELECT c
       FROM CertificateStorage c
       WHERE c.icNumber LIKE %:callNumber%
          OR :callNumber LIKE CONCAT('%', c.icNumber, '%')
          OR c.fileName LIKE %:callNumber%
          OR :callNumber LIKE CONCAT('%', c.fileName, '%')
       ORDER BY c.uploadedAt DESC
       """)
    java.util.List<CertificateStorage> findAllByCallNumber(
            @Param("callNumber") String callNumber
    );

    default Optional<CertificateStorage> findByCallNumber(String callNumber) {
        java.util.List<CertificateStorage> list = findAllByCallNumber(callNumber);
        return (list != null && !list.isEmpty()) ? Optional.of(list.get(0)) : Optional.empty();
    }
}
