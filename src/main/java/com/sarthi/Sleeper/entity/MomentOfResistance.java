package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "moment_of_resistance")
@Data
public class MomentOfResistance {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "batch_number")
        private String batchNumber;

        @Column(name = "sleeper_type")
        private String sleeperType;

        @Column(name = "bench_number")
        private String benchNumber;

        @Column(name = "sleeper_no")
        private String sleeperNo;


        @Column(name = "test_result")
        private String testResult;

        @Column(name = "remarks")
        private String remarks;

        @Column(name = "vendor_code")
        private String vendorCode;

        @Column(name = "plant_id")
        private String plantId;

        private String status;
        @Column(name = "shift")
        private String shift;

        @Column(name = "created_by")
        private Long createdBy;

        @Column(name = "created_date")
        private LocalDateTime createdDate;

        @Column(name = "updated_by")
        private Long updatedBy;

        @Column(name = "updated_date")
        private LocalDateTime updatedDate;

}
