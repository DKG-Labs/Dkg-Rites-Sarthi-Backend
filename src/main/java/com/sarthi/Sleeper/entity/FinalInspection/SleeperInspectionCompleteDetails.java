package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SLEEPER_INSPECTION_COMPLETE_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SleeperInspectionCompleteDetails {

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
