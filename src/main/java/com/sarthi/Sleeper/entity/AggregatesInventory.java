package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "aggregates_inventory")
@Data
public class AggregatesInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_receipt")
    private LocalDate dateOfReceipt;

    @Column(name = "grade_spec")
    private String gradeSpec;

    private String source;

    @Column(name = "challan_number")
    private String challanNumber;

    @Column(name = "challan_date")
    private LocalDate challanDate;

    @Column(name = "total_qty_received")
    private Double totalQtyReceived;

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