package com.sarthi.SRailPad.entity.inspectionCall;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "RAIL_INSPECTION_COMPLETE_DETAILS")
@Data
public class RailInspectionCompleteDetails {

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
