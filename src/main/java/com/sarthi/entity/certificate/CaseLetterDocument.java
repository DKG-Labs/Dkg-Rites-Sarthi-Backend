package com.sarthi.entity.certificate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "CASE_LETTER_DOCUMENT", indexes = {
        @Index(name = "idx_case_letter_call_status", columnList = "CALL_NO, STATUS"),
        @Index(name = "idx_case_letter_call_mod", columnList = "CALL_NO, MODULE_TYPE")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseLetterDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CALL_NO", nullable = false)
    private String callNo;

    @Column(name = "IC_NUMBER")
    private String icNumber;

    @Column(name = "MODULE_TYPE", nullable = false, length = 50)
    private String moduleType; // ERC, SLEEPER, RAILPAD

    @Column(name = "STAGE", length = 50)
    private String stage; // ER, EP, EF, SF, RPP, RPF

    @Column(name = "ORIGINAL_FILE_NAME")
    private String originalFileName;

    @Column(name = "BLOB_FILE_NAME", length = 1000)
    private String blobFileName;

    @Column(name = "BLOB_URL", length = 1000)
    private String blobUrl;

    @Column(name = "FILE_SIZE_ORIGINAL")
    private Long fileSizeOriginal;

    @Column(name = "FILE_SIZE_COMPRESSED")
    private Long fileSizeCompressed;

    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Column(name = "MERGED_DOC_COUNT")
    private Integer mergedDocCount;

    @Column(name = "UPLOADED_BY")
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @Column(name = "STATUS", length = 30)
    @Builder.Default
    private String status = "ACTIVE";
}
