package com.sarthi.entity.IBS;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sarthi_ibs_poi_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SarthiIbsPoiMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poi_code", nullable = false, length = 100)
    private String poiCode;

    @Column(name = "ibs_vendor_code", length = 50)
    private String ibsVendorCode;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}