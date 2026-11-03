package com.sarthi.SRailPad.dto.scrap;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/** Add or edit a Rail Pad scrap verification. The IE is taken from the login token, not from this body. */
@Data
public class RailScrapVerificationRequest {

    private String plantId;
    private String productType;
    private LocalDate scrappingDate;
    private String shift;
    private Integer qtyScrapped;
    private Boolean declarationAccepted;
    private String remarks;

    /** New photos to upload (base64). On edit, sending photos of a type replaces that type's photos. */
    private List<Photo> beforePhotos;
    private List<Photo> afterPhotos;

    /** Required on edit and delete: why the entry is being changed (audited) */
    private String reason;

    @Data
    public static class Photo {
        private String fileName;
        private String base64Data;
    }
}
