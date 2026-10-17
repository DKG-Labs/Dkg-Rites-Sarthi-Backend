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

    @org.springframework.data.jpa.repository.Query("""
        SELECT a FROM IcAnnexureDocument a
        WHERE (a.status IS NULL OR UPPER(a.status) = 'ACTIVE')
          AND (
               a.callNo = :callNo
            OR a.callNo = REPLACE(:callNo, '-', '/')
            OR a.callNo = REPLACE(:callNo, '/', '-')
            OR a.icNumber = :callNo
            OR a.icNumber = REPLACE(:callNo, '-', '/')
            OR a.icNumber = REPLACE(:callNo, '/', '-')
            OR a.callNo LIKE CONCAT('%', :callNo, '%')
            OR a.icNumber LIKE CONCAT('%', :callNo, '%')
          )
        ORDER BY a.uploadedAt DESC
        """)
    List<IcAnnexureDocument> findActiveAnnexuresByCallVariants(@org.springframework.data.repository.query.Param("callNo") String callNo);

    @org.springframework.data.jpa.repository.Query("""
        SELECT a FROM IcAnnexureDocument a
        WHERE (a.status IS NULL OR UPPER(a.status) = 'ACTIVE')
          AND (a.callNo IN :tokens OR a.icNumber IN :tokens)
        ORDER BY a.uploadedAt DESC
        """)
    List<IcAnnexureDocument> findActiveAnnexuresByCallTokens(@org.springframework.data.repository.query.Param("tokens") java.util.Collection<String> tokens);
}
