package com.sarthi.entity.IBS;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ibs_call_registration")
@Data
public class IbsCallRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callNumber;

    private String reason;
    private String status;

    private LocalDateTime acknowledgedAt;
}