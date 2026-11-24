package com.sarthi.Sms.entity.ndt;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "ndt_shift_summary")
public class NdtShiftSummaryEntity {

    @Id
    @Column(name = "duty_id")
    private String dutyId;

    @Column(name = "rolling_table_speed", nullable = false, precision = 10, scale = 2)
    private BigDecimal rollingTableSpeed;

    @Column(name = "total_rails_inspected", nullable = false)
    private Integer totalRailsInspected;

    @Column(name = "total_ut_marked_rails")
    private Integer totalUtMarkedRails;

    @Column(name = "total_ect_marked_rails")
    private Integer totalEctMarkedRails;

    @Column(name = "body_straightness_result_a", nullable = false)
    private Integer bodyStraightnessResultA;

    @Column(name = "body_straightness_result_a_1", nullable = false)
    private Integer bodyStraightnessResultA1;

    @Column(name = "body_straightness_result_marked", nullable = false)
    private Integer bodyStraightnessResultMarked;

    @Column(name = "total_profile_marked")
    private Integer totalProfileMarked;

    @Column(name = "rail_length")
    private Integer railLength;

    @Column(name = "total_ut_ok_rails")
    private Integer totalUtOkRails;

    @Column(name = "total_ut_suspected_rails")
    private Integer totalUtSuspectedRails;

    @Column(name = "total_ut_rejected_rails")
    private Integer totalUtRejectedRails;

    @Column(name = "total_recycled_rails")
    private Integer totalRecycledRails;

    @Column(name = "ect_suspected_rails")
    private Integer ectSuspectedRails;

    @Column(name = "remarks", nullable = false, length = 50)
    private String remarks;
}
