package com.sarthi.SRailPad.entity.raipadMapping;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "railpad_pincode_poi_mapping")
@Data
public class RailPadPincodePoIMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pin_code", length = 10, nullable = false)
    private String pinCode;

    @Column(name = "company_name", length = 255, nullable = false)
    private String companyName;

    @Column(name = "cin", length = 50)
    private String cin;

    @Column(name = "unit_name", length = 255)
    private String unitName;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "poi_code", length = 50, unique = true)
    private String poiCode;

    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

}