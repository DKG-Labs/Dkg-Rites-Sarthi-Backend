package com.sarthi.Sleeper.entity.FInalCall;

import aj.org.objectweb.asm.commons.Remapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ie_batch_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IEBatchSummary {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "batch_no")
        private String batchNo;

        private String callNo;

        @Column(name = "date_casted")
        private LocalDate dateCasted;

        private BigDecimal casted;
        private BigDecimal offeredPrev;
        private BigDecimal offeredNow;

        private BigDecimal passed;
        private BigDecimal rejected;

        private BigDecimal totalOffered;
        private BigDecimal totalAccepted;
        private BigDecimal totalRejected;

        private String shift;

        @Column(name = "plant_id")
        private String plantId;

        @Column(name = "vendor_code")
        private String vendorCode;

        private String createdBy;
        private String updatedBy;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinalGoodSleepers> goodSleepers;

        @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<FinalCallRejectedSleeper> rejectedSleepers;

        @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<FinalCallETSleeper> etSleepers;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinalMFSleeper> mfSleepers;

        @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<FinalInspectionRejection> finalRejections;


}
