package com.sarthi.entity.IBS;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ibs_bill_details")
@Data
public class IbsBillDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ibs_call_registration_id")
    private Long ibsCallRegistrationId;

    @Column(name = "bill_no")
    private String billNo;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "invoice_date")
    private LocalDateTime invoiceDate;

    @Column(name = "case_no")
    private String caseNo;

    @Column(name = "call_date")
    private LocalDateTime callDate;

    @Column(name = "call_sno")
    private Integer callSno;

    @Column(name = "bk_no")
    private String bkNo;

    @Column(name = "set_no")
    private String setNo;

    @Column(name = "invoice_pdf", columnDefinition = "TEXT")
    private String invoicePdf;

    @Column(name = "invoice_supp_docs", columnDefinition = "TEXT")
    private String invoiceSuppDocs;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
