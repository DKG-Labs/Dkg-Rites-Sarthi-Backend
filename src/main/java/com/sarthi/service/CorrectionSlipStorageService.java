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

    ResponseEntity<Resource> viewPdf(String callNo);

    ResponseEntity<Resource> downloadPdf(String callNo);
}
