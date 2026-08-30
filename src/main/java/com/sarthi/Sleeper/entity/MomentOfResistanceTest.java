package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "moment_of_resistance_test")
@Data
public class MomentOfResistanceTest {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "batch_number")
        private String batchNumber;

        @Column(name = "sleeper_type")
        private String sleeperType;

        @Column(name = "casting_date")
        private String castingDate;

        @Column(name = "bench_number")
        private String benchNumber;

        @Column(name = "sleeper_no")
        private String sleeperNo;

        @Column(name = "vendor_code")
        private String vendorCode;

        @Column(name = "plant_id")
        private String plantId;

        @Column(name = "shift")
        private String shift;

        @Column(name = "test_result")
        private String testResult;

        @Column(name = "created_by")
        private Long createdBy;

        @Column(name = "created_date")
        private LocalDateTime createdDate;

        @Column(name = "updated_by")
        private Long updatedBy;

        @Column(name = "updated_date")
        private LocalDateTime updatedDate;


        @OneToMany(mappedBy = "mrTest", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<MomentOfResistanceDetail> details;

}
