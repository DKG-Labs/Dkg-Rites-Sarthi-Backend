package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "INSPECTION_COMPLETE_DETAILS")
@Data
public class InspectionCompleteDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CALL_NO")
    private String callNo;

    @Column(name = "PO_NO")
    private String poNo;

    @Column(name = "CERTIFICATE_NO")
    private String certificateNo;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name = "SOURCE_IC_NO", length = 100)
    private String sourceIcNo;

    @Column(name = "DIVERSION_REQUEST_NO", length = 50)
    private String diversionRequestNo;

    @Column(name = "IS_DIVERTED_IC")
    private Boolean isDivertedIc = false;
}

