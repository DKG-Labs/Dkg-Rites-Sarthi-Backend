package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ie_vendor_calibration_inspection")
@Data
@EqualsAndHashCode(callSuper = true)
public class IeVendorCalibrationInspection extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false, length = 100)
    private String callNo;

    @Column(name = "po_number", length = 100)
    private String poNumber;

    @Column(name = "vendor_code", nullable = false, length = 100)
    private String vendorCode;

    @OneToMany(mappedBy = "inspection",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<IeVendorCalibrationInspectionDetail> details = new ArrayList<>();
}
