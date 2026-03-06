package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="production_longline_gang")
@Data
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
