package com.sarthi.SRailPad.entity.inspectionCall;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "rail_inspection_call")
public class RailInspectionCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", unique = true)
    private String callNo;

    @Column(name = "po_no")
    private String poNo;

    /** PO item serial number, e.g. "001". Maps to po_item.item_sr_no */
    @Column(name = "po_sr")
    @JsonAlias({"poSrNo", "poSr"})
    private String poSr;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "rail_pad_type")
    @JsonAlias({"railPadType", "rail_pad_type", "productType"})
    private String railPadType;

    @Column(name = "total_qty")
    @JsonAlias({"totalOfferedQty", "totalRequiredQty"})
    private Integer totalQty;

    @Column(name = "no_of_sets")
    @JsonAlias({"noOfSets", "no_of_sets"})
    private Integer noOfSets;

    @Column(name = "no_of_lots")
    private Integer noOfLots;

    @Column(name = "inspection_date")
    @JsonAlias({"desiredInspectionDate"})
    private LocalDate inspectionDate;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "call_type")
    @JsonAlias({"inspectionCallType"})
    private String callType = "FINAL";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "drawing_no")
    @JsonAlias({"drawingNo", "drawing_no", "ncrgrspType"})
    private String drawingNo;

    @Column(name = "process_ic_no", length = 500)
    @JsonProperty("processInspectionCertNo")
    @JsonAlias({"processIcNo", "processInspectionCertNo"})
    private String processIcNo;

    @Column(name = "remarks", columnDefinition = "TEXT")
    @JsonAlias({"remarks", "remark"})
    private String remarks;

    // ---- Material diversion (see SRailPad.diversion) ----
    /** FRESH / DIVERTED (chosen by the vendor when raising the call). */
    @Column(name = "call_nature", length = 20)
    @JsonAlias({"callNature", "call_nature"})
    private String callNature;

    /** FRESH / DIVERTED / DIVERTED_PASSED (DIVERTED_PASSED = system call created on diversion approval). */
    @Column(name = "call_category", length = 30)
    @JsonAlias({"callCategory", "call_category"})
    private String callCategory;

    @Column(name = "is_diverted_material")
    @JsonAlias({"isDivertedMaterial", "is_diverted_material"})
    private Boolean isDivertedMaterial;

    @Column(name = "diversion_request_no", length = 50)
    @JsonAlias({"diversionRequestNo", "diversion_request_no"})
    private String diversionRequestNo;

    /** For a DIVERTED call: the diverted-passed call whose IC it is raised on. */
    @Column(name = "source_ic_no", length = 100)
    @JsonAlias({"sourceIcNo", "source_ic_no"})
    private String sourceIcNo;

    @Transient
    private String vendorName;

    @Transient
    private String scrCode;

    @Transient
    private String rlyPoSrNo;

    @Transient
    private String uom;

    @Transient
    private Integer qtyOnOrder;

    @Transient
    private Integer qtyAcceptedTillNow;

    @Transient
    private Integer qtyDesiredForFinal;

    @Transient
    private Integer qtyDue;

    @Transient
    private LocalDate productionInitiationDate;

    @Transient
    private String ieAssignedName;

    @Transient
    private String ieContactNo;

    @Transient
    private String ieMobile;

    @Transient
    private String latestAction;

    @Transient
    private String jobStatus;

    @Transient
    private String workflowStatus;

    @Transient
    private String subStatus;

    @Transient
    private String displayStatus;

    @Transient
    private LocalDate scheduledDate;

    @Transient
    private String scheduleStatus;

    @Transient
    private Boolean isIcGenerated;

    @Transient
    private Integer offeredQty;

    @Transient
    private Integer acceptedQty;

    @Transient
    private Integer rejectedQty;

    @Transient
    private String icNo;

    @Transient
    private String icDate;

    @OneToMany(mappedBy = "inspectionCall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RailInspectionLot> lots;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (createdBy == null) createdBy = 1L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
