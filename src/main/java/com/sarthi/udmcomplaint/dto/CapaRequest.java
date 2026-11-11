package com.sarthi.udmcomplaint.dto;

import lombok.Data;

import java.util.List;

/** CAPA (optional evidence PDF is the multipart "file" part). */
@Data
public class CapaRequest {
    private List<String> correctiveActions;
    private List<String> preventiveActions;
    private String vendorCompliance;   // Y | N
    private String capaImplemented;    // Y | N
    private String remarks;
}
