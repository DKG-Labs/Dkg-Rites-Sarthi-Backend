package com.sarthi.service;

import com.sarthi.dto.StoreCorrectionSlipRequestDTO;
import com.sarthi.dto.StoreCorrectionSlipResponseDTO;
import com.sarthi.entity.certificate.CorrectionSlipDocument;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface CorrectionSlipStorageService {

    StoreCorrectionSlipResponseDTO compressAndStore(StoreCorrectionSlipRequestDTO request);

    Optional<CorrectionSlipDocument> getLatestDocument(String callNo);

    java.util.List<CorrectionSlipDocument> getAllDocuments(String callNo);

    /** Active correction slips for a call excluding unfinished pre-sign copies. */
    java.util.List<CorrectionSlipDocument> getIssuedDocuments(String callNo);

    ResponseEntity<Resource> viewPdf(String callNo);

    ResponseEntity<Resource> downloadPdf(String callNo);

    ResponseEntity<Resource> viewPdfById(Long id);

    ResponseEntity<Resource> downloadPdfById(Long id);

    byte[] getDocumentBytes(CorrectionSlipDocument doc);

    byte[] getMergedIcAndCorrectionSlips(String callNo);

    void deleteCorrectionSlip(String callNo);
}
