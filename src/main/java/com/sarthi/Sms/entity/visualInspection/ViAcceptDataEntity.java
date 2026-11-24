package com.sarthi.Sms.entity.visualInspection;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="visual_inspection_accept_data")
public class ViAcceptDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acceptance_id")
    private Integer acceptanceId;

    @Column(name = "rail_id", nullable = false)
    private String railId;

    @Column(name = "accepted_length", nullable = false)
    private BigDecimal acceptedLength;

    @Column(name = "accepted_number", nullable = false)
    private Integer acceptedNo;

    @Column(name = "rail_class", nullable = false, length = 50)
    private String railClass;
}
