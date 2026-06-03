package com.sarthi.SRailPad.entity.ieVerification;

import com.sarthi.SRailPad.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "rail_raw_material_weighment")
public class RailRawMaterialWeighment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "rail_pad_type", nullable = false)
    private String railPadType;

    @Column(name = "batch_no", nullable = false)
    private String batchNo;

    @Column(name = "total_weight", nullable = false)
    private Double totalWeight;

    @Column(name = "accepted_materials", nullable = false)
    private String acceptedMaterials;

    @Column(name = "contract_specification", nullable = false)
    private String contractSpecification;

    @Column(name = "rubber_percentage", nullable = false)
    private Double rubberPercentage;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @OneToMany(mappedBy = "weighment", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RailRawMaterialWeighmentItem> materials;

    @PrePersist
    protected void onCreate() {
        setCreatedDate(java.time.LocalDateTime.now());
        setUpdatedDate(java.time.LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedDate(java.time.LocalDateTime.now());
    }
}
