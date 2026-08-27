package com.sarthi.SRailPad.entity.ieVerification;

import com.sarthi.SRailPad.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "rail_final_load_test")
public class RailFinalLoadTest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "lot_no", nullable = false)
    private String lotNo;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "shift")
    private String shift;

    @Column(name = "railpad_type")
    private String railpadType;

    @Column(name = "offered_qty")
    private Integer offeredQty;

    @Column(name = "date_of_shift")
    private LocalDate dateOfShift;

    // Load Tonnes (Rows 1 to 8)
    @Column(name = "load1") private String load1;
    @Column(name = "load2") private String load2;
    @Column(name = "load3") private String load3;
    @Column(name = "load4") private String load4;
    @Column(name = "load5") private String load5;
    @Column(name = "load6") private String load6;
    @Column(name = "load7") private String load7;
    @Column(name = "load8") private String load8;

    // Pad 1 L & R
    @Column(name = "pad1_l1") private String pad1L1;
    @Column(name = "pad1_l2") private String pad1L2;
    @Column(name = "pad1_l3") private String pad1L3;
    @Column(name = "pad1_l4") private String pad1L4;
    @Column(name = "pad1_l5") private String pad1L5;
    @Column(name = "pad1_l6") private String pad1L6;
    @Column(name = "pad1_l7") private String pad1L7;
    @Column(name = "pad1_l8") private String pad1L8;
    @Column(name = "pad1_r1") private String pad1R1;
    @Column(name = "pad1_r2") private String pad1R2;
    @Column(name = "pad1_r3") private String pad1R3;
    @Column(name = "pad1_r4") private String pad1R4;
    @Column(name = "pad1_r5") private String pad1R5;
    @Column(name = "pad1_r6") private String pad1R6;
    @Column(name = "pad1_r7") private String pad1R7;
    @Column(name = "pad1_r8") private String pad1R8;

    // Pad 2 L & R
    @Column(name = "pad2_l1") private String pad2L1;
    @Column(name = "pad2_l2") private String pad2L2;
    @Column(name = "pad2_l3") private String pad2L3;
    @Column(name = "pad2_l4") private String pad2L4;
    @Column(name = "pad2_l5") private String pad2L5;
    @Column(name = "pad2_l6") private String pad2L6;
    @Column(name = "pad2_l7") private String pad2L7;
    @Column(name = "pad2_l8") private String pad2L8;
    @Column(name = "pad2_r1") private String pad2R1;
    @Column(name = "pad2_r2") private String pad2R2;
    @Column(name = "pad2_r3") private String pad2R3;
    @Column(name = "pad2_r4") private String pad2R4;
    @Column(name = "pad2_r5") private String pad2R5;
    @Column(name = "pad2_r6") private String pad2R6;
    @Column(name = "pad2_r7") private String pad2R7;
    @Column(name = "pad2_r8") private String pad2R8;

    // Marginal Pad 1 L & R
    @Column(name = "m_pad1_l1") private String mPad1L1;
    @Column(name = "m_pad1_l2") private String mPad1L2;
    @Column(name = "m_pad1_l3") private String mPad1L3;
    @Column(name = "m_pad1_l4") private String mPad1L4;
    @Column(name = "m_pad1_l5") private String mPad1L5;
    @Column(name = "m_pad1_l6") private String mPad1L6;
    @Column(name = "m_pad1_l7") private String mPad1L7;
    @Column(name = "m_pad1_l8") private String mPad1L8;
    @Column(name = "m_pad1_r1") private String mPad1R1;
    @Column(name = "m_pad1_r2") private String mPad1R2;
    @Column(name = "m_pad1_r3") private String mPad1R3;
    @Column(name = "m_pad1_r4") private String mPad1R4;
    @Column(name = "m_pad1_r5") private String mPad1R5;
    @Column(name = "m_pad1_r6") private String mPad1R6;
    @Column(name = "m_pad1_r7") private String mPad1R7;
    @Column(name = "m_pad1_r8") private String mPad1R8;

    // Marginal Pad 2 L & R
    @Column(name = "m_pad2_l1") private String mPad2L1;
    @Column(name = "m_pad2_l2") private String mPad2L2;
    @Column(name = "m_pad2_l3") private String mPad2L3;
    @Column(name = "m_pad2_l4") private String mPad2L4;
    @Column(name = "m_pad2_l5") private String mPad2L5;
    @Column(name = "m_pad2_l6") private String mPad2L6;
    @Column(name = "m_pad2_l7") private String mPad2L7;
    @Column(name = "m_pad2_l8") private String mPad2L8;
    @Column(name = "m_pad2_r1") private String mPad2R1;
    @Column(name = "m_pad2_r2") private String mPad2R2;
    @Column(name = "m_pad2_r3") private String mPad2R3;
    @Column(name = "m_pad2_r4") private String mPad2R4;
    @Column(name = "m_pad2_r5") private String mPad2R5;
    @Column(name = "m_pad2_r6") private String mPad2R6;
    @Column(name = "m_pad2_r7") private String mPad2R7;
    @Column(name = "m_pad2_r8") private String mPad2R8;

    // Marginal Pad 3 L & R
    @Column(name = "m_pad3_l1") private String mPad3L1;
    @Column(name = "m_pad3_l2") private String mPad3L2;
    @Column(name = "m_pad3_l3") private String mPad3L3;
    @Column(name = "m_pad3_l4") private String mPad3L4;
    @Column(name = "m_pad3_l5") private String mPad3L5;
    @Column(name = "m_pad3_l6") private String mPad3L6;
    @Column(name = "m_pad3_l7") private String mPad3L7;
    @Column(name = "m_pad3_l8") private String mPad3L8;
    @Column(name = "m_pad3_r1") private String mPad3R1;
    @Column(name = "m_pad3_r2") private String mPad3R2;
    @Column(name = "m_pad3_r3") private String mPad3R3;
    @Column(name = "m_pad3_r4") private String mPad3R4;
    @Column(name = "m_pad3_r5") private String mPad3R5;
    @Column(name = "m_pad3_r6") private String mPad3R6;
    @Column(name = "m_pad3_r7") private String mPad3R7;
    @Column(name = "m_pad3_r8") private String mPad3R8;

    // Marginal Pad 4 L & R
    @Column(name = "m_pad4_l1") private String mPad4L1;
    @Column(name = "m_pad4_l2") private String mPad4L2;
    @Column(name = "m_pad4_l3") private String mPad4L3;
    @Column(name = "m_pad4_l4") private String mPad4L4;
    @Column(name = "m_pad4_l5") private String mPad4L5;
    @Column(name = "m_pad4_l6") private String mPad4L6;
    @Column(name = "m_pad4_l7") private String mPad4L7;
    @Column(name = "m_pad4_l8") private String mPad4L8;
    @Column(name = "m_pad4_r1") private String mPad4R1;
    @Column(name = "m_pad4_r2") private String mPad4R2;
    @Column(name = "m_pad4_r3") private String mPad4R3;
    @Column(name = "m_pad4_r4") private String mPad4R4;
    @Column(name = "m_pad4_r5") private String mPad4R5;
    @Column(name = "m_pad4_r6") private String mPad4R6;
    @Column(name = "m_pad4_r7") private String mPad4R7;
    @Column(name = "m_pad4_r8") private String mPad4R8;

    @Column(name = "load_status")
    private String loadStatus;

    @Column(name = "not_ok_count")
    private Integer notOkCount;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @PrePersist
    protected void onCreate() {
        setCreatedDate(java.time.LocalDateTime.now());
        setUpdatedDate(java.time.LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedDate(java.time.LocalDateTime.now());
    }
}
