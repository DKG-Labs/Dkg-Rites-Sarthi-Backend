package com.sarthi.Sms.dto.vi;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ViAcptRejReportDto {
    private String date;
    private String shift;
    private String inspectionWorkspot;
    private String railGrade;
    private String railSection;
    private BigDecimal stdOfferedLengthForInspection;
    private Long railsOfferedForInspection;
    private Long classA130m;
    private Long classA117m;
    private Long classA104m;
    private Long classA86_67m;
    private Long classA65m;
    private Long classA52m;
    private Long classA26m;
    private Long classA25m;
    private Long classA24m;
    private Long classA18m;
    private Long classA17m;
    private Long classA16m;
    private Long classA13m;
    private Long classA12m;
    private Long classA11m;
    private Long classA10m;

    // New numeric aggregates
    private Long classAPlus01Total; // total pieces in A+0.1
    private Long totalAcceptedPieces; // A + A+0.1 (sum of pieces)
    private BigDecimal totalAcceptedMeters; // sum(length * pieces) for A and A+0.1
    private BigDecimal totalAcceptedTonnes; // derived from meters * kg/m

    private Long recycleCutBar;
    private Long recycleRefinish;
    private Long rejection13m;
    private Long rejection12m;
    private Long rejection11m;
    private Long rejection10m;
    private Long rejectionComponentLength;
}
