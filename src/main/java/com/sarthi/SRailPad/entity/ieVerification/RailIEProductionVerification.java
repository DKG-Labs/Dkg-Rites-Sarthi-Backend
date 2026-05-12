package com.sarthi.SRailPad.entity.ieVerification;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "rail_ie_production_verification")
public class RailIEProductionVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "shift")
    private String shift;

    @Column(name = "production_unit")
    private String productionUnit;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "total_pieces_produced")
    private Integer totalPiecesProduced;

    @Column(name = "total_pieces_rejected")
    private Integer totalPiecesRejected;

    @Column(name = "total_accepted_pieces")
    private Integer totalAcceptedPieces;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RailIEProductionInfo> productionInfos;

    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RailIEProductionRejection> rejections;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
