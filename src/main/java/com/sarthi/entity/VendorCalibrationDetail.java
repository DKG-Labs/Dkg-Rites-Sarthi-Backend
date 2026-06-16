package com.sarthi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Entity representing vendor_calibration_details table.
 * Stores individual calibration row records linked to a parent header.
 */
@Entity
@Table(name = "vendor_calibration_details")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "vendorCalibrationHeader")
public class VendorCalibrationDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_name", nullable = false, length = 150)
    private String instrumentName;

    @Column(name = "capacity", length = 100)
    private String capacity;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "used_for", length = 255)
    private String usedFor;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "calibration_certificate_no", length = 100)
    private String calibrationCertificateNo;

    @Column(name = "calibration_date")
    private LocalDate calibrationDate;

    @Column(name = "calibration_due_date")
    private LocalDate calibrationDueDate;

    @Column(name = "certifying_lab_name", length = 150)
    private String certifyingLabName;

    @Column(name = "accreditation_agency", length = 50)
    private String accreditationAgency;

    @Column(name = "make_model", length = 150)
    private String makeModel;

    @Column(name = "master_equip_no_cert_validity", length = 255)
    private String masterEquipNoCertValidity;

    @Column(name = "master_equip_nabl_details", length = 255)
    private String masterEquipNablDetails;

    @Column(name = "notification_days")
    private Integer notificationDays = 30;

    @Column(name = "calibration_status", length = 50)
    private String calibrationStatus = "Valid";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    @JsonIgnore
    private VendorCalibrationHeader vendorCalibrationHeader;
}
