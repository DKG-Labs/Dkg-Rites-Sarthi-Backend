package com.sarthi.entity.ercdiversion;

import com.sarthi.entity.BaseAuditEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "erc_material_diversion_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErcDiversionItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diversion_request_id", nullable = false)
    @JsonBackReference
    private ErcDiversionRequest diversionRequest;

    @Column(name = "heat_no", length = 50)
    private String heatNo;

    @Column(name = "tc_no", length = 50)
    private String tcNo;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    @Column(name = "accepted_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal acceptedQty;

    @Column(name = "downstream_consumed_qty", precision = 14, scale = 3, nullable = false)
    @Builder.Default
    private BigDecimal downstreamConsumedQty = BigDecimal.ZERO;

    @Column(name = "available_balance_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal availableBalanceQty;

    @Column(name = "diversion_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal diversionQty;

    @Column(name = "remaining_balance_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal remainingBalanceQty;
}
