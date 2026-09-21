package com.sarthi.repository.certificate;

import com.sarthi.entity.certificate.IcAnnexureDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IcAnnexureDocumentRepository extends JpaRepository<IcAnnexureDocument, Long> {

    List<IcAnnexureDocument> findByCallNoAndStatusOrderByUploadedAtDesc(String callNo, String status);

    List<IcAnnexureDocument> findByCallNoAndModuleTypeAndStatusOrderByUploadedAtDesc(String callNo, String moduleType, String status);

    List<IcAnnexureDocument> findByIcNumberAndStatusOrderByUploadedAtDesc(String icNumber, String status);

    Optional<IcAnnexureDocument> findByIdAndStatus(Long id, String status);
}
