package com.sarthi.Sms.dto.vi;

import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViDefectRecordDto {
    private String date;
    private Integer inspectionWorkspot;
    private String railSection;
    private String railGrade;
    private BigDecimal stdOfferedLengthForInspection;
    private Long noOfRailsOfferedForInspection;
    private BigDecimal nmi;
    private BigDecimal mdf;
    private BigDecimal mdm;
    private BigDecimal hs;
    private BigDecimal ls;
    private BigDecimal hh;
    private BigDecimal lh;
    private BigDecimal nf;
    private BigDecimal wf;
    private BigDecimal oht;
    private BigDecimal uht;
    private BigDecimal tnw;
    private BigDecimal tkw;
    private BigDecimal lap;
    private BigDecimal gm;
    private BigDecimal br;
    private BigDecimal kk;
    private BigDecimal tw;
    private BigDecimal cRc;
    private BigDecimal ns;
    private BigDecimal ol;
    private BigDecimal sl;
    private BigDecimal os;
    private BigDecimal nhn;
    private BigDecimal asy;
    private BigDecimal be;
    private BigDecimal us;
    private BigDecimal ds;
    private BigDecimal ut;
    private BigDecimal tfPositive;
    private BigDecimal tfNegative;
    private BigDecimal hfPositive;
    private BigDecimal hfNegative;
    private BigDecimal cp;
    private BigDecimal fbc;
    private BigDecimal fbcx;
    private BigDecimal hr;
    private BigDecimal others;
    private Long total;
    private String dateForSteelMillFinishUT;
    private String mill;
    private String finish;
    private BigDecimal utStandardOfferedLength;
}
