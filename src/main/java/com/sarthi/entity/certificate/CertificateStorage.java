package com.sarthi.entity.certificate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "CERTIFICATE_STORAGE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "IC_NUMBER", unique = true, nullable = false)
    private String icNumber;

    @Column(name = "BLOB_URL", length = 1000)
    private String blobUrl;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "UPLOADED_BY")
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;
}
