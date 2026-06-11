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
       """)
    Optional<CertificateStorage> findByCallNumber(
            @Param("callNumber") String callNumber
    );
}
