package com.sarthi.Sms.dto.rolling;

// import java.time.LocalDateTime;

// import jakarta.persistence.Column;
// import jakarta.persistence.EmbeddedId;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import lombok.Data;

// @Data
// @Entity
// @Table(name="rolling_test_sample_acceptance")
// public class TestSampleAcptEntity {

//     private TestSampleAcptId tsai;

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     @Column(name="rolling_test_sample_id")
//     private Integer id;

//     @Column(name = "rail_grade", length = 50)
//     private String railGrade;

//     @Column(name = "chemical", nullable = false)
//     private Boolean chemical = false;

//     @Column(name = "n2", nullable = false)
//     private Boolean n2 = false;

//     @Column(name = "fwt_st", nullable = false)
//     private Boolean fwtSt = false;

//     @Column(name = "mechanical", nullable = false)
//     private Boolean mechanical = false;

//     @Column(name = "sp", nullable = false)
//     private Boolean sp = false;

//     @Column(name = "ir", nullable = false)
//     private Boolean ir = false;

//     @Column(name = "o2", nullable = false)
//     private Boolean o2 = false;

//     @Column(name = "fwt_hs", nullable = false)
//     private Boolean fwtHs = false;

//     @Column(name = "fwt_st_sr", nullable = false)
//     private Boolean fwtStSr = false;

//     @Column(name = "tensile_foot", nullable = false)
//     private Boolean tensileFoot = false;

//     @Column(name = "micro", nullable = false)
//     private Boolean micro = false;

//     @Column(name = "decarb", nullable = false)
//     private Boolean decarb = false;

//     @Column(name = "rsh", nullable = false)
//     private Boolean rsh = false;

//     @Column(name = "tensile", nullable = false)
//     private Boolean tensile = false;

//     @Column(name = "ph", nullable = false)
//     private Boolean ph = false;

//     @Column(name = "created_at", nullable = false)
//     private LocalDateTime createdAt = LocalDateTime.now();
// }

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "rolling_test_sample_acceptance")
@NoArgsConstructor
@AllArgsConstructor
// @Builder
public class TestSampleAcptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="rolling_test_sample_id")
    private Integer id;

    @Column(name = "rail_grade")
    private String railGrade;

    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Column(name = "sample_id", length = 50)
    private String sampleId;

    @Column(name = "heat_number", length = 50)
    private String heatNo;

    @Column(name = "strand")
    private Integer strand;

    @Column(name = "sample_lot", length = 50)
    private String sampleLot;

    @Column(name = "chemical", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean chemical;


    @Column(name = "n2", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean n2;

    @Column(name = "fwt_st", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean fwtSt;


    @Column(name = "mechanical", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean mechanical;

    @Column(name = "sp", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean sp;

    @Column(name = "ir", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean ir;

    @Column(name = "o2", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean o2;


    @Column(name = "fwt_hs", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean fwtHs;

    @Column(name = "fwt_st_sr", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean fwtStSr;

    @Column(name = "tensile_foot", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean tensileFoot;

    @Column(name = "micro", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean micro;

    @Column(name = "decarb", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean decarb;

    @Column(name = "rsh", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean rsh;

    @Column(name = "ph", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean ph;

    @Column(name = "tensile", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean tensile;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
