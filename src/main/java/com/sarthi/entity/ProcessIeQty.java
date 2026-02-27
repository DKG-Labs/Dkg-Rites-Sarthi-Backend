package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(
        name = "PROCESS_IE_QTY",
        uniqueConstraints = @UniqueConstraint(columnNames = {"REQUEST_ID", "SWIFT_CODE"})
)
@Data
public class ProcessIeQty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "REQUEST_ID", nullable = false)
    private String requestId;     // IC / Process ID

    @Column(name = "SWIFT_CODE", nullable = false)
    private String swiftCode;     // SWIFT_A, SWIFT_B, SWIFT_C

    @Column(name = "IE_USER_ID", nullable = false)
    private Integer ieUserId;     // IE who inspected

    @Column(name = "INSPECTED_QTY", nullable = false)
    private Integer inspectedQty;

    @Column(name = "COMPLETED")
    private Boolean completed = false;


    private String lotNumber;
    private int offeredQty;
    private int manufactureQty;

    private String heatNo;
    private BigDecimal rejectedQty;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate = new Date();
}