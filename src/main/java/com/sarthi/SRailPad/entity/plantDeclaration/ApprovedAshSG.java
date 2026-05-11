package com.sarthi.SRailPad.entity.plantDeclaration;

import com.sarthi.SRailPad.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity(name = "RailApprovedAshSG")
@Table(name = "rail_approved_ash_sg")
@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovedAshSG extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "pad_type")
    private String padType;

    @Column(name = "ash_content_a")
    private Double ashContentA;

    @Column(name = "specific_gravity_a")
    private Double specificGravityA;

    @Column(name = "ash_content_b")
    private Double ashContentB;

    @Column(name = "specific_gravity_b")
    private Double specificGravityB;

    @Column(name = "approval_ref_no")
    private String approvalRefNo;

    @Column(name = "approval_date")
    private LocalDate approvalDate;
}
