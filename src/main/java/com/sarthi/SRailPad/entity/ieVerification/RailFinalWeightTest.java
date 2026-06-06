package com.sarthi.SRailPad.entity.ieVerification;

import com.sarthi.SRailPad.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "rail_final_weight_test")
public class RailFinalWeightTest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "lot_no", nullable = false)
    private String lotNo;

    @Column(name = "railpad_type")
    private String railpadType;

    @Column(name = "offered_qty")
    private Integer offeredQty;

    @Column(name = "date_of_shift")
    private LocalDate dateOfShift;

    @Column(name = "n1")
    private Integer n1;

    @Column(name = "ac1")
    private Integer ac1;

    @Column(name = "re1")
    private Integer re1;

    @Column(name = "n2")
    private Integer n2;

    @Column(name = "ac2")
    private Integer ac2;

    @Column(name = "re2")
    private Integer re2;

    @Column(name = "min_weight")
    private Double minWeight;

    @Column(name = "max_weight")
    private Double maxWeight;

    @Column(name = "is_second_active")
    private Boolean isSecondActive;

    @Column(name = "weight_status")
    private String weightStatus;

    @Column(name = "not_ok1")
    private Integer notOk1;

    @Column(name = "not_ok2")
    private Integer notOk2;

    @Column(name = "total_not_ok")
    private Integer totalNotOk;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "railFinalWeightTest", fetch = FetchType.LAZY)
    private List<RailFinalWeightTestSample> samples;

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
