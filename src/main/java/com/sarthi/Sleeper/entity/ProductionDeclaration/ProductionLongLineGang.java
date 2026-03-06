package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;

@Entity
@Table(name="production_longline_gang")
public class ProductionLongLineGang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mode;

    private Integer gangFrom;

    private Integer gangTo;

    private Integer gangNo;

    private String sleeperType;

    private Integer mouldsPerGang;

    @ManyToOne
    @JoinColumn(name="declaration_id")
    private ProductionDeclaration declaration;

}
