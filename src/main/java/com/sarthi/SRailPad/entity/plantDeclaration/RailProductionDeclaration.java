package com.sarthi.SRailPad.entity.plantDeclaration;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "RailProductionDeclaration")
@Table(name = "rail_production_declaration")
@Data
public class RailProductionDeclaration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "plant_id")
    private String plantId;

    private String shift;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "production_line")
    private String productionLine;
    
    @Column(name = "po_no")
    private String poNo;


    private String status;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RailProductionProduct> products;
}
