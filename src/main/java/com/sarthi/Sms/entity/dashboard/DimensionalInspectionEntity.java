package com.sarthi.Sms.entity.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="dimensional_inspection")
public class DimensionalInspectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id")
    private int inspectionId;

    @Column(name = "rail_id", nullable = false, length = 100)
    private String railId;

    @Column(name = "camera_id", nullable = false)
    private int cameraId;

    @Column(name = "base_image_path", nullable = false, length = 255)
    private String baseImagePath;

    @Column(name = "edge_diff", precision = 10, scale = 3)
    private BigDecimal edgeDiff;

    @Column(name = "chop_diff", precision = 10, scale = 3)
    private BigDecimal chopDiff;

    @Column(name = "image_diff", precision = 10, scale = 3)
    private BigDecimal imageDiff;

    @Column(name = "actual_status", length = 10)
    private String actualStatus;

    @Column(name = "result_status", length = 10)
    private String resultStatus;

    @Column(name = "confusion_classifier", length = 10)
    private String confusionClassifier;

    @Column(name = "inspected_at", nullable = false)
    private LocalDateTime inspectedAt;

    @Column(name = "operator_id", length = 255)
    private String operatorId;

    @Column(name = "duty_id", length = 255)
    private String dutyId;

    @Column(name = "shift", length = 5)
    private String shift;

    @Lob
    @Column(name = "defect_type", columnDefinition = "JSON")
    private String defectType;

    @Column(name = "distance_from_head", precision = 10, scale = 3)
    private BigDecimal distanceFromHead;

    @Column(name = "dimension_deviation", precision = 10, scale = 5)
    private BigDecimal dimensionDeviation;

    @Column(name = "no_of_dimension_variation")
    private Integer noOfDimensionVariation;

    @Column(name = "camera_name", length = 50)
    private String cameraName;
}
