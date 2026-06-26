package com.sarthi.SRailPad.entity.inspectionCall;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rail_process_call_details")
public class RailProcessCallDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_call_id", nullable = false)
    private RailInspectionCall inspectionCall;

    @Column(name = "drawing_no")
    private String drawingNo;

    @Column(name = "uom")
    private String uom;

    @Column(name = "qty_on_order")
    private Integer qtyOnOrder;

    @Column(name = "qty_accepted_till_now")
    private Integer qtyAcceptedTillNow;

    @Column(name = "qty_desired_for_final")
    private Integer qtyDesiredForFinal;

    @Column(name = "qty_due")
    private Integer qtyDue;

    @Column(name = "production_initiation_date")
    private LocalDate productionInitiationDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

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
