package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dowel_inventory")
@Data
public class DowelInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_receipt")
    private LocalDate dateOfReceipt;

    @Column(name = "grade_type")
    private String gradeType;

    private String manufacturer;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "rites_ic_number")
    private String ritesIcNumber;

    @Column(name = "rites_ic_date")
    private LocalDate ritesIcDate;

    @Column(name = "total_qty_received")
    private Integer totalQtyReceived;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    private String vendorCode;
    private String plantId;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}