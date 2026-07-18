package com.sarthi.entity.IBS;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ibs_payment_details")
@Data
public class IbsPaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ibs_call_registration_id")
    private Long ibsCallRegistrationId;

    @Column(name = "case_no")
    private String caseNo;

    @Column(name = "call_recv_dt")
    private LocalDateTime callRecvDt;

    @Column(name = "call_sno")
    private Integer callSno;

    @Column(name = "description")
    private String description;

    @Column(name = "mer_txn_id")
    private String merTxnId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "cust_email")
    private String custEmail;

    @Column(name = "cust_mobile")
    private String custMobile;

    @Column(name = "txn_complete_date")
    private OffsetDateTime txnCompleteDate;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
