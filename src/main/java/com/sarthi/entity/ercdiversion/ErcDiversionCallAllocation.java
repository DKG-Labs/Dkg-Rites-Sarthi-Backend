package com.sarthi.entity.ercdiversion;

import com.sarthi.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "erc_diversion_call_allocation")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErcDiversionCallAllocation extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", nullable = false)
    private Long callId;

    @Column(name = "call_no", nullable = false, length = 50)
    private String callNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    private ErcDivertedPassedBasket basket;

    @Column(name = "allocated_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal allocatedQty;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "RESERVED"; // RESERVED, RELEASED, CONSUMED_IN_IC
}
