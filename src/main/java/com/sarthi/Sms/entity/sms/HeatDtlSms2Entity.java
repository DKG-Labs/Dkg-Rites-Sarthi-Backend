package com.sarthi.Sms.entity.sms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name="heat_detail_sms2")
public class HeatDtlSms2Entity {
    @Id
    @Column(name = "heat_number", nullable = false, length = 50)
    private String heatNo;

    @Column(name = "heat_stage", nullable = false, length = 50)
    private String heatStage;

    @Column(name = "heat_remark", length = 50)
    private String heatRemark;

    @Column(name = "turn_down_temp")
    private Integer turnDownTemp;

    @Column(name = "turn_down_temp_wv")
    private String turnDownTempWv;

    @Column(name = "degassing_vacuum", precision = 10, scale = 2)
    private BigDecimal degassingVacuum;

    @Column(name = "degassing_vacuum_wv")
    private String degassingVacuumWv;

    @Column(name = "degassing_duration", precision = 10, scale = 2)
    private Integer degassingDuration;
    
    @Column(name = "degassing_duration_wv", precision = 10, scale = 2)
    private String degassingDurationWv;

    @Column(name = "casting_temp")
    private Integer castingTemp;

    @Column(name = "casting_temp_2")
    private Integer castingTemp2;

    @Column(name = "caster_number", length = 10)
    private String casterNo;

    @Column(name = "sequence_number", length = 10)
    private String sequenceNo;

    @Column(name = "hydris", precision = 10, scale = 2)
    private BigDecimal hydris;

    @Column(name = "is_probe_dipped", nullable = false)
    private Boolean isProbeDipped = false;

    @Column(name = "is_hydrogen_bw_80_and_100", nullable = false)
    private Boolean isHydrogenBw80And100 = false;

    @Column(name = "nitrogen", precision = 10, scale = 4)
    private BigDecimal nitrogen;

    @Column(name = "oxygen", precision = 10, scale = 2)
    private BigDecimal oxygen;

    @Column(name = "number_of_prime_blooms")
    private Integer noOfPrimeBlooms;

    @Column(name = "prime_blooms_length", precision = 10, scale = 2)
    private BigDecimal primeBloomsLength;

    @Column(name = "prime_blooms_total_length", precision = 10, scale = 2)
    private BigDecimal primeBloomsTotalLength;

    @Column(name = "number_of_co_blooms")
    private Integer noOfCoBlooms;

    @Column(name = "co_blooms_length", precision = 10, scale = 2)
    private BigDecimal coBloomsLength;

    @Column(name = "co_blooms_total_length", precision = 10, scale = 2)
    private BigDecimal coBloomsTotalLength;

    @Column(name = "number_of_rejected_blooms")
    private Integer noOfRejectedBlooms;

    @Column(name = "rejected_blooms_length", precision = 10, scale = 2)
    private BigDecimal rejectedBloomsLength;

    @Column(name = "rejected_blooms_total_length", precision = 10, scale = 2)
    private BigDecimal rejectedBloomsTotalLength;

    @Column(name = "weight_of_prime_blooms", precision = 10, scale = 2)
    private BigDecimal weightOfPrimeBlooms;

    @Column(name = "weight_of_co_blooms", precision = 10, scale = 2)
    private BigDecimal weightOfCoBlooms;

    @Column(name = "weight_of_rejected_blooms", precision = 10, scale = 2)
    private BigDecimal weightOfRejectedBlooms;

    @Column(name = "total_cast_wt", precision = 10, scale = 2)
    private BigDecimal totalCastWt;

    @Column(name = "is_diverted", nullable = false)
    private Boolean isDiverted = false;

    @Column(name = "sent_to_ladle", length = 50)
    private String sentToLadle;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="other_remark", length = 50)
    private String otherRemark;
}
