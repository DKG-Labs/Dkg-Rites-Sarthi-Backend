package com.sarthi.SRailPad.entity.plantDeclaration;

import com.sarthi.SRailPad.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity(name = "RailRawMaterialSource")
@Table(name = "rail_raw_material_source")
@Data
@EqualsAndHashCode(callSuper = true)
public class RawMaterialSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "material_name")
    private String materialName;

    @Column(name = "material_type")
    private String materialType;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "doc_ref_no")
    private String docRefNo;

    @Column(name = "doc_date")
    private LocalDate docDate;
}
