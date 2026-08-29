package com.sarthi.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pincode_poi_mapping")
@Data
public class PincodePoIMapping {

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

    private String vendorCode;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(name = "contact_person_number", length = 20)
    private String contactPersonNumber;
}

