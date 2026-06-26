package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_images", indexes = {
    @Index(name = "idx_inspection_images_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_inspection_images_type", columnList = "type_of_call")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class InspectionImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "inspection_call_no", nullable = false)
    private String inspectionCallNo;

    // "RM", "PROCESS", or "FINAL"
    @Column(name = "type_of_call", nullable = false)
    private String typeOfCall;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "shift")
    private String shift;

    @Column(name = "date_of_inspection")
    private String dateOfInspection;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
