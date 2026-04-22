package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "et_epoxy_treated_sleeper")
@Data
public class EpoxyTreatedSleeper {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String location;
        private LocalDate dateOfCasting;
        private String batchNumber;
        private String sleeperType;

        private String remark;
        private Boolean isConfirmed;

        private String shift;

        private String vendorCode;
        private String plantId;

        private Long createdBy;
        private LocalDateTime createdDate;

        private Long updatedBy;
        private LocalDateTime updatedDate;


        @OneToMany(mappedBy = "et", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<EtSleeperDetails> sleepers;

}