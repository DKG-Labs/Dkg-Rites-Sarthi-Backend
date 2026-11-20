package com.sarthi.SRailPad.diversion.entity;

import com.sarthi.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Qty a diverted RPP/RPF call has reserved from a basket row. */
@Entity
@Table(name = "rail_diversion_call_allocation")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = "basket")
@ToString(exclude = "basket")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RailDiversionCallAllocation extends BaseAuditEntity {

    public static final String RESERVED = "RESERVED";
    public static final String RELEASED = "RELEASED";
    public static final String CONSUMED_IN_IC = "CONSUMED_IN_IC";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false, length = 50)
    private String callNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    private RailDivertedPassedBasket basket;

    @Column(name = "allocated_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal allocatedQty;

    @Column(name = "consumed_qty", precision = 14, scale = 3, nullable = false)
    @Builder.Default
    private BigDecimal consumedQty = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = RESERVED;
}
