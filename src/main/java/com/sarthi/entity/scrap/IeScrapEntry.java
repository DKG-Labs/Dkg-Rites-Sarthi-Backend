package com.sarthi.entity.scrap;

import com.sarthi.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "ie_scrap_entry")
@Data
@EqualsAndHashCode(callSuper = false)
public class IeScrapEntry extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "poi_code", nullable = false, length = 50)
    private String poiCode;

    @Column(name = "unit_line_shed_no", nullable = false, length = 100)
    private String unitLineShedNo;

    @Column(name = "scrapping_date", nullable = false)
    private LocalDate scrappingDate;

    @Column(name = "shift", nullable = false, length = 20)
    private String shift;

    @Column(name = "no_of_pieces_scrapped", nullable = false)
    private Integer noOfPiecesScrapped;

    @Column(name = "pending_qty_at_submit", nullable = false)
    private Integer pendingQtyAtSubmit;

    @Column(name = "declaration_accepted", nullable = false)
    private Boolean declarationAccepted = false;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "scrap_verification_id", nullable = false, unique = true, length = 50)
    private String scrapVerificationId;

    @Column(name = "ie_user_id", nullable = false)
    private Integer ieUserId;

    @Column(name = "ie_type", nullable = false, length = 20)
    private String ieType;

    @Column(name = "status", nullable = false, length = 10)
    private String status = "ACTIVE";
}
