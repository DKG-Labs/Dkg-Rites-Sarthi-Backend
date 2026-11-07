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
    // Process inspection split by UOM (from rail_process_inspection_result)
    private long qtyInspectedNos = 0;
    private long qtyAcceptedNos = 0;
    private long qtyInspectedSets = 0;
    private long qtyAcceptedSets = 0;
    // Final inspection IC qty split by UOM (from rail_final_inspection_lot_results)
    private long icIssuedQtyNos = 0;
    private long icIssuedQtySets = 0;

    // Process rejections (Nos) by IE production-verification dropdown reasons,
    // from rail_process_inspection_batch
    private long shortMoulding = 0;
    private long bubblesBlisters = 0;
    private long surfaceRoughness = 0;
    private long nylonCordExposure = 0;
    private long unevenEdges = 0;
    private long uncleanCutSides = 0;
    private long blowHoles = 0;
    private long porosity = 0;
    private long improperGrooves = 0;
    private long otherProcessRejection = 0;
    private long totalProcessRejection = 0;

    // Final (acceptance) test rejections: testKey → not-ok count, all lots of the PO's calls
    private java.util.Map<String, Long> finalRejections = new java.util.LinkedHashMap<>();
    private long totalFinalRejection = 0;
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
