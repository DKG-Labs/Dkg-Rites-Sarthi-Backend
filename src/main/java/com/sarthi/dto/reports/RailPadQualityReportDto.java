package com.sarthi.dto.reports;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailPadQualityReportDto {
    private int sNo;
    private String zonalRailway = "";
    private String vendor = "";
    private String typeOfRubberPad = "";
    private String poNo = "";
    private String poDate = "";
    private String specification = "";
    private long totalPoQty = 0;
    private String uom = "";
    private long qtyInspected = 0;
    private long qtyAccepted = 0;
    private long icIssuedQty = 0;
    private String lastDateIcIssued = "";

    // Process Defects
    private long rawMaterialCheck = 0;
    private long compounding = 0;
    private long mixing = 0;
    private long curing = 0;
    private long cutting = 0;
    private long rheometer = 0;
    private long visualCheckFinishing = 0;

    // Acceptance Defects
    private long hardness = 0;
    private long specificGravity = 0;
    private long rubberContent = 0;
    private long ashContent = 0;
    private long reboundResilience = 0;
    private long dimension = 0;
    private long weight = 0;
    private long surfaceDefect = 0;
    private long compressionSet = 0;
    private long visualTest = 0;
    private long otherRejection = 0;

    private String remarks = "";
    private double rejectionPercent = 0.0;
}
