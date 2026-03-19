package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sleeper_inspection_call")
@Data
public class SleeperInspectionCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", unique = true)
    private String callNo;

    @Column(name = "po_no", nullable = false)
    private String poNo;

    @Column(name = "sr_no", nullable = false)
    private String srNo;

    @Column(name = "sleeper_type", nullable = false)
    private String sleeperType;

    @Column(name = "total_offered", nullable = false)
    private Integer totalOffered;

    @Column(name = "total_rejected", nullable = false)
    private Integer totalRejected;

    @Column(name = "status")
    private String status = "Pending for verification";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "inspectionCall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SleeperInspectionCallBatch> batchesSelected;
}
