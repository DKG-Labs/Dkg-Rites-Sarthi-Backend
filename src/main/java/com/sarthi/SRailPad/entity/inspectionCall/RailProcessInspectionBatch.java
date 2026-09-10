package com.sarthi.SRailPad.entity.inspectionCall;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity(name = "RailProcessInspectionBatch")
@Table(name = "rail_process_inspection_batch")
@Data
public class RailProcessInspectionBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RailProcessInspectionResult result;

    @Column(name = "declaration_batch_id")
    private Long declarationBatchId; // Links back to RailProductionBatch id if needed

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "drawing_no")
    private String drawingNo;

    @Column(name = "reason_for_rejection", columnDefinition = "TEXT")
    private String reasonForRejection;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "qty_manufactured")
    private Integer qtyManufactured;

    @Column(name = "qty_rejected")
    private Integer qtyRejected;

    @Column(name = "qty_accepted")
    private Integer qtyAccepted;
}
