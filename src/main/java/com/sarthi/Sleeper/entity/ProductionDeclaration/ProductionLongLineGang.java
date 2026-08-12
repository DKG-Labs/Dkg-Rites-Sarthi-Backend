package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="production_longline_gang")
@Data
public class ProductionLongLineGang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mode;

    private String gangFrom;

    private String gangTo;

    private String gangNo;

    private String sleeperType;

    private Integer mouldsPerGang;

    private String sleeperCategory;

    private Integer totalSleepers;

    private Double rft;

    private java.time.LocalTime lbcTime;

    @ManyToOne
    @JoinColumn(name="declaration_id")
    private ProductionDeclaration declaration;

   // @OneToMany(mappedBy = "gang", cascade = CascadeType.ALL)
    //private List<ProductionSleeper> sleepers;
   @org.hibernate.annotations.BatchSize(size = 100)
   @OneToMany(mappedBy = "gang", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<ProductionSleeper> sleepers = new ArrayList<>();
}
