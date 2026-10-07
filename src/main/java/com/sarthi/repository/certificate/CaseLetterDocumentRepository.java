package com.sarthi.repository.certificate;

import com.sarthi.entity.certificate.CaseLetterDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseLetterDocumentRepository extends JpaRepository<CaseLetterDocument, Long> {

    List<CaseLetterDocument> findByCallNoAndStatus(String callNo, String status);

    Optional<CaseLetterDocument> findFirstByCallNoAndStatusOrderByUploadedAtDesc(String callNo, String status);

    List<CaseLetterDocument> findByCallNo(String callNo);
}
