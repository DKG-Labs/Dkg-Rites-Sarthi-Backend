package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ie_fields_mapping")
@Data
public class IEFieldsMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pin_code", length = 10, nullable = false)
    private String pinCode;

    @Column(name = "product", length = 50, nullable = false)
    private String product;

    @Column(name = "stage", length = 50, nullable = false)
    private String stage;

    @Column(name = "rio", length = 50, nullable = false)
    private String rio;

    private String plantPincode;
}
