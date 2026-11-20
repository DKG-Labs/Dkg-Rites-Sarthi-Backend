package com.sarthi.SRailPad.diversion.entity;

import com.sarthi.entity.BaseAuditEntity;
import com.sarthi.enums.BasketStatus;
import com.sarthi.enums.DiversionStage;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Approved diverted quantity available on the target PO. Unlike ERC, this is actually consumed:
 * diverted calls reserve from it, IC issue consumes, cancel/withdraw releases.
 */
@Entity
@Table(name = "rail_diverted_passed_basket")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"diversionRequest", "diversionItem"})
@ToString(exclude = {"diversionRequest", "diversionItem"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RailDivertedPassedBasket extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diversion_request_id", nullable = false)
    private RailDiversionRequest diversionRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diversion_item_id", nullable = false)
    private RailDiversionItem diversionItem;

    @Column(name = "stage", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DiversionStage stage;

    @Column(name = "vendor_code", nullable = false, length = 50)
    private String vendorCode;

    @Column(name = "plant_id", nullable = false, length = 100)
    private String plantId;

    @Column(name = "rio_id", nullable = false, length = 20)
    private String rioId;

    @Column(name = "source_po_no", nullable = false, length = 50)
    private String sourcePoNo;

    @Column(name = "source_po_sr_no", nullable = false, length = 20)
    private String sourcePoSrNo;

    @Column(name = "source_ic_no", nullable = false, length = 100)
    private String sourceIcNo;

    @Column(name = "diverted_call_no", length = 50)
    private String divertedCallNo;

    @Column(name = "target_po_no", nullable = false, length = 50)
    private String targetPoNo;

    @Column(name = "target_po_sr_no", nullable = false, length = 20)
    private String targetPoSrNo;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "approved_diverted_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal approvedDivertedQty;

    @Column(name = "allocated_call_qty", precision = 14, scale = 3, nullable = false)
    @Builder.Default
    private BigDecimal allocatedCallQty = BigDecimal.ZERO;

    @Column(name = "consumed_ic_qty", precision = 14, scale = 3, nullable = false)
    @Builder.Default
    private BigDecimal consumedIcQty = BigDecimal.ZERO;

    @Column(name = "available_balance_qty", precision = 14, scale = 3, nullable = false)
    private BigDecimal availableBalanceQty;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BasketStatus status = BasketStatus.AVAILABLE;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
