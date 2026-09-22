package com.sarthi.repository.certificate;

import com.sarthi.entity.certificate.CorrectionSlipDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorrectionSlipDocumentRepository extends JpaRepository<CorrectionSlipDocument, Long> {

    List<CorrectionSlipDocument> findByCallNoAndStatusOrderByUploadedAtDesc(String callNo, String status);

    Optional<CorrectionSlipDocument> findFirstByCallNoAndStatusOrderByUploadedAtDesc(String callNo, String status);

    Optional<CorrectionSlipDocument> findFirstByCallNoAndStageAndStatusOrderByUploadedAtDesc(String callNo, String stage, String status);

    Optional<CorrectionSlipDocument> findByIdAndStatus(Long id, String status);
}
