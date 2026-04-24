package com.sarthi.repository.certificate;

import com.sarthi.entity.certificate.CertificateStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateStorageRepository extends JpaRepository<CertificateStorage, Long> {
    Optional<CertificateStorage> findByIcNumber(String icNumber);
}
