package com.sarthi.repository.certificate;

import com.sarthi.entity.certificate.CorrectionSlipDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorrectionSlipDocumentRepository extends JpaRepository<CorrectionSlipDocument, Long> {

    List<CorrectionSlipDocument> findByCallNoAndStatusOrderByUploadedAtDesc(String callNo, String status);

    List<CorrectionSlipDocument> findByCallNoAndStatusOrderByUploadedAtAsc(String callNo, String status);

    List<CorrectionSlipDocument> findByCallNoInAndStatusOrderByUploadedAtAsc(List<String> callNos, String status);

    Optional<CorrectionSlipDocument> findFirstByCallNoAndStatusOrderByUploadedAtDesc(String callNo, String status);

    Optional<CorrectionSlipDocument> findFirstByCallNoAndStageAndStatusOrderByUploadedAtDesc(String callNo, String stage, String status);

    Optional<CorrectionSlipDocument> findByIdAndStatus(Long id, String status);

    @Query("""
       SELECT d FROM CorrectionSlipDocument d
       WHERE (
           d.callNo IN :variations
           OR d.icNumber IN :variations
           OR d.callNo LIKE CONCAT('%', :clean, '%')
           OR :clean LIKE CONCAT('%', d.callNo, '%')
           OR (d.icNumber IS NOT NULL AND d.icNumber <> '' AND (d.icNumber LIKE CONCAT('%', :clean, '%') OR :clean LIKE CONCAT('%', d.icNumber, '%')))
       )
       AND (d.status IS NULL OR UPPER(d.status) NOT IN ('DELETED', 'CANCELLED', 'INACTIVE'))
       ORDER BY d.uploadedAt ASC, d.id ASC
    """)
    List<CorrectionSlipDocument> findAllMatchingForCall(
            @Param("variations") List<String> variations,
            @Param("clean") String clean
    );
}
