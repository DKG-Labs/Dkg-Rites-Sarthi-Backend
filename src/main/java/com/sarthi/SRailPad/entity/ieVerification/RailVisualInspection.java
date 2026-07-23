package com.sarthi.SRailPad.entity.ieVerification;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "rail_visual_inspection")
public class RailVisualInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_id", nullable = false)
    private String plantId;

    @Column(name = "vendor_code", nullable = false)
    private String vendorCode;

    @Column(name = "shift")
    private String shift;

    @Column(name = "casting_date")
    private String castingDate;

    @Column(name = "time_of_check", nullable = false)
    private String timeOfCheck;

    @Column(name = "sample_quantity")
    private Integer sampleQuantity;

    @Column(name = "clear_cut_sides", nullable = false)
    private String clearCutSides;

    @Column(name = "smooth_surface", nullable = false)
    private String smoothSurface;

    @Column(name = "defect_remarks", length = 500)
    private String defectRemarks;

    @Column(name = "status")
    private String status;

    @Column(name = "timestamp")
    private String timestamp;
}
