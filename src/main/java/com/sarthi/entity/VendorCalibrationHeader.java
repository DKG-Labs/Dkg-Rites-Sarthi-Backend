package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing vendor_calibration_header table.
 * Stores parent calibration info: vendor, plant, and category.
 */
@Entity
@Table(name = "vendor_calibration_header", uniqueConstraints = {
    @UniqueConstraint(name = "uk_vendor_category", columnNames = {"vendor_code", "category"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class VendorCalibrationHeader extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_code", nullable = false, length = 50)
    private String vendorCode;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "certificate_file_path", columnDefinition = "LONGTEXT")
    private String certificateFilePath;

    @OneToMany(mappedBy = "vendorCalibrationHeader", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendorCalibrationDetail> details = new ArrayList<>();

    public void addDetail(VendorCalibrationDetail detail) {
        details.add(detail);
        detail.setVendorCalibrationHeader(this);
    }

    public void removeDetail(VendorCalibrationDetail detail) {
        details.remove(detail);
        detail.setVendorCalibrationHeader(null);
    }
}
