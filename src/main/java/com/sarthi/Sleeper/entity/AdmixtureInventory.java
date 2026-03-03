package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admixture_inventory")
@Data
public class AdmixtureInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_receipt")
    private LocalDate dateOfReceipt;

    private String manufacturer;

    @Column(name = "grade_spec")
    private String gradeSpec;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "lot_no")
    private String lotNo;

    @Column(name = "mtc_no")
    private String mtcNo;

    @Column(name = "total_quantity")
    private Double totalQuantity;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}