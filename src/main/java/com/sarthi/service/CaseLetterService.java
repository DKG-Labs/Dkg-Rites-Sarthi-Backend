package com.sarthi.service;

import com.sarthi.entity.certificate.CaseLetterDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CaseLetterService {

    Map<String, Object> getCallDocumentsMetadata(String callNo, String moduleType);

    byte[] mergePdfs(List<byte[]> pdfByteList);

    byte[] mergeUploadedFiles(List<MultipartFile> files);

    CaseLetterDocument saveCaseLetter(
            List<MultipartFile> files,
            String callNo,
            String icNumber,
            String moduleType,
            String stage,
            String uploadedBy
    );

    CaseLetterDocument getLatestCaseLetter(String callNo);

    byte[] getCaseLetterPdf(Long id);

    byte[] getLatestCaseLetterPdf(String callNo);

    boolean deleteCaseLetter(String callNo, String requestedBy);

    boolean deleteCaseLetterById(Long id, String requestedBy);
}
