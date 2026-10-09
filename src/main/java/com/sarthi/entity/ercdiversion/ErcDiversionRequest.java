package com.sarthi.entity.ercdiversion;

import com.sarthi.entity.BaseAuditEntity;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;
import com.sarthi.enums.DiversionType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "erc_material_diversion_request")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErcDiversionRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_no", unique = true, nullable = false, length = 50)
    private String requestNo;

    @Column(name = "vendor_code", nullable = false, length = 50)
    private String vendorCode;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "plant_id", nullable = false)
    private Long plantId;

    @Column(name = "plant_name", length = 150)
    private String plantName;

    @Column(name = "plant_address", length = 500)
    private String plantAddress;

    @Column(name = "rio_id", nullable = false, length = 20)
    private String rioId;

    @Column(name = "cm_user_id", nullable = false, length = 50)
    private String cmUserId;

    @Column(name = "sbu_head_user_id", length = 50)
    private String sbuHeadUserId;

    @Column(name = "stage", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DiversionStage stage;

    @Column(name = "diversion_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DiversionType diversionType;

    // Source Details
    @Column(name = "source_po_no", nullable = false, length = 50)
    private String sourcePoNo;

    @Column(name = "source_po_sr_no", nullable = false, length = 20)
    private String sourcePoSrNo;

    @Column(name = "source_ic_no", nullable = false, length = 100)
    private String sourceIcNo;

    @Column(name = "source_call_no", nullable = false, length = 50)
    private String sourceCallNo;

    @Column(name = "source_ic_date")
    private LocalDate sourceIcDate;

    // Target Details
    @Column(name = "target_po_no", nullable = false, length = 50)
    private String targetPoNo;

    @Column(name = "target_po_sr_no", nullable = false, length = 20)
    private String targetPoSrNo;

    @Column(name = "total_diversion_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal totalDiversionQty;

    @Column(name = "unit_of_measurement", length = 20)
    private String unitOfMeasurement;

    // Reason & Permission
    @Column(name = "reason_code", length = 50)
    private String reasonCode;

    @Column(name = "reason_remarks", columnDefinition = "TEXT")
    private String reasonRemarks;

    @Column(name = "railway_permission_no", nullable = false, length = 100)
    private String railwayPermissionNo;

    @Column(name = "railway_permission_date", nullable = false)
    private LocalDate railwayPermissionDate;

    @Column(name = "railway_permission_doc_url", nullable = false, length = 500)
    private String railwayPermissionDocUrl;

    @Column(name = "vendor_remarks", columnDefinition = "TEXT")
    private String vendorRemarks;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private DiversionRequestStatus status;

    @OneToMany(mappedBy = "diversionRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<ErcDiversionItem> items = new ArrayList<>();
}
