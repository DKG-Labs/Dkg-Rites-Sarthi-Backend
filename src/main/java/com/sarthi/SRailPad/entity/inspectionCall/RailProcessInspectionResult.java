package com.sarthi.SRailPad.entity.inspectionCall;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "RailProcessInspectionResult")
@Table(name = "rail_process_inspection_result")
@Data
public class RailProcessInspectionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_call_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RailInspectionCall inspectionCall;

    @Column(name = "call_qty")
    private Integer callQty;

    @Column(name = "total_manufactured_qty")
    private Integer totalManufacturedQty;

    @Column(name = "total_rejected_qty")
    private Integer totalRejectedQty;

    @Column(name = "total_accepted_qty")
    private Integer totalAcceptedQty;

    @Column(name = "reason_for_rejection", columnDefinition = "LONGTEXT")
    private String reasonForRejection;

    @Column(name = "lot_range_from")
    private String lotRangeFrom;

    @Column(name = "lot_range_to")
    private String lotRangeTo;

    @Column(name = "remarks", columnDefinition = "LONGTEXT")
    private String remarks;

    @Column(name = "inspection_start_date")
    private LocalDate inspectionStartDate;

    @Column(name = "inspection_end_date")
    private LocalDate inspectionEndDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "shift")
    private String shift;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RailProcessInspectionBatch> batches;

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
