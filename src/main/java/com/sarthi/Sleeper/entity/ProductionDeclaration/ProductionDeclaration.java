package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_declaration")
@Data
public class ProductionDeclaration {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String plantType;

        private String productionUnit;

        private LocalDate castingDate;

        private String shift;

        private String batchNumber;

        private String mixDesignReference;

        private LocalTime lbcTime;

        @Column(name = "po_no")
        private String poNo;

        // SUMMARY FIELDS

        private Integer totalCastedSleepers;

        private Integer totalSleeperTypes;

        private Double totalRft;

        private String remarks;

        private Long createdBy;

        private LocalDateTime createdDate;

        private Long updatedBy;

        private LocalDateTime updatedDate;

        private String vendorCode;
        private String plantId;

        // STRESS BENCH RELATION

     //   @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL)
     //   private List<ProductionStressChamber> chambers;

        // LONG LINE RELATION

      //  @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL)
      //  private List<ProductionLongLineGang> gangs;

        @org.hibernate.annotations.BatchSize(size = 100)
        @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ProductionStressChamber> chambers = new ArrayList<>();

        @org.hibernate.annotations.BatchSize(size = 100)
        @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ProductionLongLineGang> gangs = new ArrayList<>();


}
