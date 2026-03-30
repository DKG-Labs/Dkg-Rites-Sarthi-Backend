package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vendor_plant")
@Data
public class VendorPlant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vendorCode;
    private String companyName;
    private String plantName;
    private String plantId;


}
