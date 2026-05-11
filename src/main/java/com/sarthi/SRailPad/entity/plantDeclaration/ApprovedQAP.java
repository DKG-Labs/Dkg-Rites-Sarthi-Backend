package com.sarthi.SRailPad.entity.plantDeclaration;

import com.sarthi.SRailPad.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Entity(name = "RailApprovedQAP")
@Table(name = "rail_approved_qap")
@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovedQAP extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "qap_no")
    private String qapNo;

    @Column(name = "approving_authority")
    private String approvingAuthority;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "validity_date")
    private LocalDate validityDate;

    @OneToMany(mappedBy = "approvedQAP", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QAPProductDetail> productDetails;
}
