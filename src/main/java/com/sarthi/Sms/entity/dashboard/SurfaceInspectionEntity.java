package com.sarthi.Sms.entity.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="surface_inspection")
public class SurfaceInspectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "annotated_image", nullable = false, length = 255)
    private String annotatedImage;

    @Column(name = "rail_id", nullable = false, length = 50)
    private String railId;

    @Column(name = "shift", length = 5)
    private String shift;

    @Column(name = "distance")
    private Integer distance;

    @Column(name = "camera", nullable = false)
    private int cameraId;

    @Column(name = "label_index")
    private Integer labelIndex;

    @Column(name = "label_name", length = 255)
    private String labelName;

    @Column(name = "confidence", precision = 10, scale = 5)
    private BigDecimal confidence;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "ai_status")
    private Integer aiStatus;

    @Column(name = "admin_status")
    private Integer adminStatus;

    @Column(name = "camera_name", length = 50)
    private String cameraName;
}
