package com.sarthi.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "ie_vendor_calibration_inspection_detail")
@Data
@EqualsAndHashCode(callSuper = true)
public class IeVendorCalibrationInspectionDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_name")
    private String instrumentName;

    @Column(name = "capacity")
    private String capacity;

    @Column(name = "description")
    private String description;

    @Column(name = "used_for")
    private String usedFor;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "calibration_certificate_no")
    private String calibrationCertificateNo;

    @Column(name = "calibration_date")
    private LocalDate calibrationDate;

    @Column(name = "calibration_due_date")
    private LocalDate calibrationDueDate;

    @Column(name = "certifying_lab_name")
    private String certifyingLabName;

    @Column(name = "accreditation_agency")
    private String accreditationAgency;

    @Column(name = "notification_days")
    private Integer notificationDays;

    @Column(name = "calibration_status")
    private String calibrationStatus;

    @Column(name = "inspection_status")
    private String inspectionStatus;

    @Column(name = "inspection_remark", columnDefinition = "TEXT")
    private String inspectionRemark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id")
    private IeVendorCalibrationInspection inspection;
}