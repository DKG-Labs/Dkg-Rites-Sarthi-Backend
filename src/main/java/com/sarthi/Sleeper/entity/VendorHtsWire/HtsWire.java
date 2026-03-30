package com.sarthi.Sleeper.entity.VendorHtsWire;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hts_wire")
@Data
public class HtsWire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_receipt")
    private LocalDate dateOfReceipt;

    @Column(name = "grade_spec")
    private String gradeSpec;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "rites_ic_number")
    private String ritesIcNumber;

    @Column(name = "rites_ic_date")
    private LocalDate ritesIcDate;

    @Column(name = "relaxation_test")
    private String relaxationTest; // YES / NO

    @Column(name = "relaxation_test_date")
    private LocalDate relaxationTestDate;

    @Column(name = "total_qty_received")
    private Double totalQtyReceived;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    private String vendorCode;
    private String plantId;


    // ===== One To Many =====

    @OneToMany(mappedBy = "htsWire",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<HtsCoilDetails> coilDetails = new ArrayList<>();
}
