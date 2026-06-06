package com.sarthi.SRailPad.entity.ieVerification;

import com.sarthi.SRailPad.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "rail_final_inspection_lot_results")
public class RailFinalInspectionLotResults extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "lotResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RailFinalInspectionSectionResult> sectionResults = new ArrayList<>();

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "rly_po_sr_no")
    private String rlyPoSrNo;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "railpad_type")
    private String railpadType;

    @Column(name = "lot_no", nullable = false)
    private String lotNo;

    @Column(name = "offered_qty")
    private Integer offeredQty;

    @Column(name = "accepted_qty")
    private Integer acceptedQty;

    @Column(name = "rejected_qty")
    private Integer rejectedQty;

    @Column(name = "visual_dimensional_status")
    private String visualDimensionalStatus;

    @Column(name = "physical_ageing_properties_status")
    private String physicalAgeingPropertiesStatus;

    @Column(name = "electrical_chemical_status")
    private String electricalChemicalStatus;

    @Column(name = "dynamic_durability_test_status")
    private String dynamicDurabilityTestStatus;

    @Column(name = "ncrgrsp_status")
    private String ncrgrspStatus;

    @Column(name = "overall_status")
    private String overallStatus;

    @Column(name = "hologram")
    private String hologram;

    @Column(name = "remarks")
    private String remarks;

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
