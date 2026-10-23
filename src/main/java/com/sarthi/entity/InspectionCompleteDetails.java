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
}

