package com.sarthi.SRailPad.diversion.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sarthi.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** One lot / batch line of a Rail Pad diversion request. */
@Entity
@Table(name = "rail_material_diversion_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = "diversionRequest")
@ToString(exclude = "diversionRequest")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RailDiversionItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diversion_request_id", nullable = false)
    @JsonBackReference
    private RailDiversionRequest diversionRequest;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

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
