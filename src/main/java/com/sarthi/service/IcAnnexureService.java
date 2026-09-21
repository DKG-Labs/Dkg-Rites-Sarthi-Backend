package com.sarthi.service;

import com.sarthi.entity.certificate.IcAnnexureDocument;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IcAnnexureService {

    IcAnnexureDocument uploadAnnexure(
            MultipartFile file,
            String callNo,
            String icNumber,
            String moduleType,
            String uploadedBy
    );

    List<IcAnnexureDocument> getAnnexuresByCall(String callNo, String moduleType);

    void deleteAnnexure(Long id, String requestedBy);

    ResponseEntity<Resource> downloadAnnexure(Long id);
}
