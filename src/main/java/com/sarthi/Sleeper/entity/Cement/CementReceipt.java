package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cement_receipt")
@Data
public class CementReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_receipt")
    private LocalDate dateOfReceipt;

    @Column(name = "grade_spec")
    private String gradeSpec;

    private String manufacturer;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

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

    @OneToMany(mappedBy = "cementReceipt",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CementBatchDetails> batchDetails = new ArrayList<>();
}