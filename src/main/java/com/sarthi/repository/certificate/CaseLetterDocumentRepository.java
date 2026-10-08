package com.sarthi.repository.certificate;

import com.sarthi.entity.certificate.CaseLetterDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseLetterDocumentRepository extends JpaRepository<CaseLetterDocument, Long> {

    List<CaseLetterDocument> findByCallNoAndStatus(String callNo, String status);

    // Find the latest ACTIVE case letter handling hyphen/slash variants in a single query
    @org.springframework.data.jpa.repository.Query("""
        SELECT c FROM CaseLetterDocument c
        WHERE c.status = :status
          AND (c.callNo = :callNo
               OR c.callNo = REPLACE(:callNo, '-', '/')
               OR c.callNo = REPLACE(:callNo, '/', '-'))
        ORDER BY c.uploadedAt DESC
        """)
    java.util.Optional<CaseLetterDocument> findActiveByCallNoVariants(@org.springframework.data.repository.query.Param("callNo") String callNo,
                                                                        @org.springframework.data.repository.query.Param("status") String status);


    // Retained for backward compatibility; not used in new flow
    Optional<CaseLetterDocument> findFirstByCallNoAndStatusOrderByUploadedAtDesc(String callNo, String status);

    List<CaseLetterDocument> findByCallNo(String callNo);
}
