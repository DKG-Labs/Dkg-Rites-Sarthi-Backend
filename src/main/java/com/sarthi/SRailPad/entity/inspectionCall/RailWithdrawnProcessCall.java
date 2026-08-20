package com.sarthi.SRailPad.entity.inspectionCall;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rail_withdrawn_process_calls")
public class RailWithdrawnProcessCall {

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

    @Column(name = "withdrawn_by")
    private String withdrawnBy;

    @Column(name = "withdrawn_remarks", columnDefinition = "TEXT")
    private String withdrawnRemarks;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "original_data_json", columnDefinition = "LONGTEXT")
    private String originalDataJson;

    @PrePersist
    protected void onCreate() {
        if (withdrawnAt == null) {
            withdrawnAt = LocalDateTime.now();
        }
    }
}
