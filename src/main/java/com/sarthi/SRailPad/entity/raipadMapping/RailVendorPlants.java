package com.sarthi.SRailPad.entity.raipadMapping;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rail_vendor_plant")
@Data
public class RailVendorPlants {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "vendor_code", length = 100)
        private String vendorCode;

        @Column(name = "vendor_id")
        private Long vendorId;

        @Column(name = "company_name", length = 255)
        private String companyName;

        @Column(name = "plant_name", length = 255)
        private String plantName;

        @Column(name = "plant_id", length = 100)
        private String plantId;

        @Column(name = "plant_pincode", length = 20)
        private String plantPincode;

        @Column(name = "rio", length = 20)
        private String rio;

        @Column(name = "zonal_railway", length = 50)
        private String zonalRailway;

        @Column(name = "contact_person", length = 255)
        private String contactPerson;

        @Column(name = "contact_person_number", length = 20)
        private String contactPersonNumber;

        @Column(name = "status", length = 50)
        private String status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
