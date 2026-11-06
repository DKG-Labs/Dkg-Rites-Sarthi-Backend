package com.sarthi.Sleeper.entity.scrap;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sleeper scrap verification: the IE witnessed the permanent destruction of specific rejected sleepers.
 * Each row is a debit in the scrap ledger; the sleepers it covers are in sleeper_scrap_verification_item.
 */
@Data
@Entity
@Table(name = "sleeper_scrap_verification")
public class SleeperScrapVerification {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DELETED = "DELETED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SSV-yyMMdd-NN */
    @Column(name = "verification_no", nullable = false, length = 30, unique = true)
    private String verificationNo;

    /** vendor_plant.plant_id without the leading ':' */
    @Column(name = "plant_id", nullable = false, length = 100)
    private String plantId;

    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "plant_name")
    private String plantName;

    @Column(name = "scrapping_date", nullable = false)
    private LocalDate scrappingDate;

    @Column(name = "shift", length = 20)
    private String shift;

    /** Pending sleepers in the basket at the moment of submission */
    @Column(name = "available_pending_qty", nullable = false)
    private Integer availablePendingQty;

    /** Number of sleepers witnessed (= active items) */
    @Column(name = "qty_scrapped", nullable = false)
    private Integer qtyScrapped;

    @Column(name = "declaration_accepted", nullable = false)
    private Boolean declarationAccepted;

    @Column(name = "declaration_text", nullable = false, length = 500)
    private String declarationText;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "ie_user_id", nullable = false)
    private Long ieUserId;

    @Column(name = "ie_name")
    private String ieName;

    /** PROCESS_IE or MAIN_IE */
    @Column(name = "ie_type", nullable = false, length = 20)
    private String ieType;

    /** ACTIVE or DELETED (soft delete) */
    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
        if (status == null) status = STATUS_ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
