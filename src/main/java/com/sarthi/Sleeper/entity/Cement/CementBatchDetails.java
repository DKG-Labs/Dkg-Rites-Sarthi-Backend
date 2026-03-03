package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cement_batch_details")
@Data
public class CementBatchDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_no")
    private Integer weekNo;

    @Column(name = "year_no")
    private Integer yearNo;

    @Column(name = "mtc_no")
    private String mtcNo;

    @Column(name = "quantity_kg")
    private Double quantityKg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cement_receipt_id")
    private CementReceipt cementReceipt;
}