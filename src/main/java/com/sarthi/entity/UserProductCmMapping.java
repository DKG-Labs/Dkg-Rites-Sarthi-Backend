package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "user_product_cm_mapping")
@Data
public class UserProductCmMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmployeeCode;

    private String productType; // ERC / SLEEPER / RAILPAD

    private String cmEmployeeCode;

    private Long createdBy;

    private Date createdDate;


}