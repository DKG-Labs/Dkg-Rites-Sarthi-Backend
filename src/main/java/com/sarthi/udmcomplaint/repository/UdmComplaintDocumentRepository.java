package com.sarthi.udmcomplaint.repository;

import com.sarthi.udmcomplaint.entity.UdmComplaintDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UdmComplaintDocumentRepository extends JpaRepository<UdmComplaintDocument, Long> {

    List<UdmComplaintDocument> findByUdmRejectionIdAndActiveTrueOrderByUploadedAtAsc(Long udmRejectionId);

    Optional<UdmComplaintDocument> findByIdAndActiveTrue(Long id);
}
