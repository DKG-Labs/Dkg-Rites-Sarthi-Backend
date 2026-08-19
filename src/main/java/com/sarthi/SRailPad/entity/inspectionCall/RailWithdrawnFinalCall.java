package com.sarthi.SRailPad.entity.inspectionCall;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rail_withdrawn_final_calls")
public class RailWithdrawnFinalCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "po_no")
    private String poNo;

    @Column(name = "po_sr")
    private String poSr;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "rail_pad_type")
    private String railPadType;

    @Column(name = "drawing_no")
    private String drawingNo;

    @Column(name = "total_qty")
    private Integer totalQty;

    @Column(name = "no_of_lots")
    private Integer noOfLots;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "process_ic_no", length = 500)
    private String processIcNo;

    @Column(name = "withdrawn_by")
    private String withdrawnBy;

    @Column(name = "withdrawn_remarks", columnDefinition = "TEXT")
    private String withdrawnRemarks;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "batch_numbers", columnDefinition = "TEXT")
    private String batchNumbers;

    @Column(name = "sub_drawing_no", columnDefinition = "TEXT")
    private String subDrawingNo;

    @Column(name = "lots_and_batches_json", columnDefinition = "LONGTEXT")
    private String lotsAndBatchesJson;

    @Column(name = "original_data_json", columnDefinition = "LONGTEXT")
    private String originalDataJson;

    @PrePersist
    protected void onCreate() {
        if (withdrawnAt == null) {
            withdrawnAt = LocalDateTime.now();
        }
    }
}
