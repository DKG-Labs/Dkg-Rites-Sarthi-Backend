USE sarthidb;

SELECT 
    CONCAT(COALESCE(vp.plant_name, vp.company_name, pd.plant_id), ' (', pd.plant_id, ')') AS `Manufacturing Plant`,
    COALESCE(vp.zonal_railway, ph.rly_short_name, ph.rly_cd, 'N/A') AS `Zonal Railway`,
    COALESCE(swt.rio, 'NRIO') AS `RIO`,
    cs.IC_NUMBER AS `IC Number`,
    sic.call_no AS `Call No`,
    pd.batch_number AS `Batch Offered`,
    COALESCE(ph.item_cat_descr, 'PSC Mainline Sleeper') AS `Type of Sleeper`,
    
    -- Quantities
    COALESCE(pd.total_casted_sleepers, 0) AS `Total Batch Casted`,
    COALESCE(gs.batch_offered_qty, pd.total_casted_sleepers, 0) AS `No. of Sleeper Offered`,
    
    -- Accepted = Offered - Total Rejected
    GREATEST(0, COALESCE(gs.batch_offered_qty, pd.total_casted_sleepers, 0) - (
        COALESCE(def.total_final_rejected, 0) + 
        COALESCE(dem.total_demoulding_rejected, 0) + 
        COALESCE(mie.total_main_ie_rejected, 0)
    )) AS `No. of Sleeper Accepted`,

    -- Total Rejected
    NULLIF(
        COALESCE(def.total_final_rejected, 0) + 
        COALESCE(dem.total_demoulding_rejected, 0) + 
        COALESCE(mie.total_main_ie_rejected, 0), 0
    ) AS `No. of Sleeper Rejected`,

    -- =========================================================
    -- CRITICAL DIMENSION COLUMNS
    -- =========================================================
    NULLIF(crit.rail_seat_slope, 0) AS `Critical: Rail Seat Slope`,
    NULLIF(crit.location_of_inserts, 0) AS `Critical: Location of Inserts in Turn Outs`,
    NULLIF(crit.rail_seat, 0) AS `Critical: Rail Seat`,
    NULLIF(crit.toe_gap, 0) AS `Critical: Toe Gap`,
    NULLIF(crit.location_of_dowel, 0) AS `Critical: Location of Dowel`,
    NULLIF(crit.angularity_in_inserts, 0) AS `Critical: Angularity in Inserts`,
    NULLIF(crit.camber_turnout, 0) AS `Critical: Camber in Turn Out Sleepers`,

    -- =========================================================
    -- VISUAL INSPECTION COLUMNS
    -- =========================================================
    NULLIF(vis.surface_defect, 0) AS `Visual: Surface Defect`,
    NULLIF(vis.honeycomb, 0) AS `Visual: Honeycomb`,
    NULLIF(vis.crack, 0) AS `Visual: Crack`,
    NULLIF(vis.insert_defect, 0) AS `Visual: Insert Missing / Tilt / Sink`,
    NULLIF(vis.dowel_defect, 0) AS `Visual: Dowel Missing / Tilt / Sink`,
    NULLIF(vis.wire_slippage, 0) AS `Visual: Wire Slippage`,
    NULLIF(vis.ftc_defect, 0) AS `Visual: FTC / NFTC Defect`,

    -- =========================================================
    -- NON-CRITICAL DIMENSION COLUMNS
    -- =========================================================
    NULLIF(dim.outer_gauge, 0) AS `Non Critical: Outer Gauge`,
    NULLIF(dim.height_gauge, 0) AS `Non Critical: Height Gauge`,
    NULLIF(dim.wind_gauge, 0) AS `Non Critical: Wind Gauge`,
    NULLIF(dim.length_of_sleeper, 0) AS `Non Critical: Length of Sleeper`,
    NULLIF(dim.width_of_sleeper, 0) AS `Non Critical: Width of Sleeper`,

    -- =========================================================
    -- DEMOULDING REASON COLUMNS
    -- =========================================================
    NULLIF(dem.dem_surface_defect, 0) AS `Demoulding: Surface Defect`,
    NULLIF(dem.dem_honeycomb, 0) AS `Demoulding: Honeycomb`,
    NULLIF(dem.dem_crack, 0) AS `Demoulding: Crack`,
    NULLIF(dem.dem_insert_defect, 0) AS `Demoulding: Insert Missing / Tilt / Sink`,
    NULLIF(dem.dem_dowel_defect, 0) AS `Demoulding: Dowel Missing / Tilt / Sink`,
    NULLIF(dem.dem_wire_slippage, 0) AS `Demoulding: Wire Slippage`,
    NULLIF(dem.dem_outer_gauge, 0) AS `Demoulding: Outer Gauge`,
    NULLIF(dem.dem_rail_seat, 0) AS `Demoulding: Rail Seat`,
    NULLIF(dem.dem_toe_gap, 0) AS `Demoulding: Toe Gap`,
    NULLIF(dem.dem_slope, 0) AS `Demoulding: Rail Seat Slope`,
    NULLIF(dem.dem_height_gauge, 0) AS `Demoulding: Height Gauge`,
    NULLIF(dem.dem_length_sleeper, 0) AS `Demoulding: Length of Sleeper`,

    -- =========================================================
    -- MAIN IE REASON COLUMNS
    -- =========================================================
    NULLIF(mie.mie_surface_defect, 0) AS `Main IE: Surface Defect / Broken Edge`,
    NULLIF(mie.mie_honeycomb, 0) AS `Main IE: Honeycomb`,
    NULLIF(mie.mie_crack, 0) AS `Main IE: Crack`,
    NULLIF(mie.mie_insert_defect, 0) AS `Main IE: Insert Missing / Tilt / Sink`,
    NULLIF(mie.mie_dowel_defect, 0) AS `Main IE: Dowel Missing / Tilt / Sink`,
    NULLIF(mie.mie_wire_slippage, 0) AS `Main IE: Wire Slippage`,
    NULLIF(mie.mie_outer_gauge, 0) AS `Main IE: Outer Gauge / Dim Variation`,
    NULLIF(mie.mie_slope, 0) AS `Main IE: Rail Seat Slope`,
    NULLIF(mie.mie_toe_gap, 0) AS `Main IE: Toe Gap`,
    NULLIF(mie.mie_inserts_turnout, 0) AS `Main IE: Location of Inserts in Turn Outs`,
    NULLIF(mie.mie_angularity, 0) AS `Main IE: Angularity in Inserts`,
    NULLIF(mie.mie_general_reject, 0) AS `Main IE: Other Rejection Reason`

FROM production_declaration pd
JOIN sleeper_inspection_call_batch sicb 
     ON CONVERT(pd.batch_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sicb.batch_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
JOIN sleeper_inspection_call sic 
     ON sicb.inspection_call_id = sic.id
    AND (
        CONVERT(pd.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        OR CONVERT(REPLACE(pd.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(sic.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
    )

LEFT JOIN (
    SELECT batch_id, COUNT(*) AS batch_offered_qty
    FROM sleeper_ic_good_sleepers
    GROUP BY batch_id
) gs ON gs.batch_id = sicb.id

JOIN (
    SELECT request_id, MAX(workflow_transition_id) AS max_id
    FROM sleeper_workflow_transaction
    WHERE workflow_id = 2
    GROUP BY request_id
) latest_swt ON CONVERT(latest_swt.request_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
JOIN sleeper_workflow_transaction swt 
     ON swt.workflow_transition_id = latest_swt.max_id 
    AND UPPER(COALESCE(swt.job_status, '')) = 'IC_GENERATION'
JOIN sleeper_inspection_complete_details sicd 
     ON CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sicd.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
JOIN CERTIFICATE_STORAGE cs 
     ON (
         CONVERT(cs.IC_NUMBER USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sicd.certificate_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
         OR CONVERT(cs.IC_NUMBER USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
     )
LEFT JOIN vendor_plant vp 
     ON (
         CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(pd.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
         OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(pd.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
     )
LEFT JOIN po_header ph 
     ON (
         CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
         OR CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.po_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
     )

-- Stage Test Rejections
LEFT JOIN (
    SELECT ith.batch_id, COUNT(DISTINCT itr.sleeper_no) AS total_final_rejected
    FROM inspection_test_header ith
    JOIN inspection_test_result itr ON ith.id = itr.test_header_id
    WHERE itr.result = 'REJECTED' AND itr.active = 1
    GROUP BY ith.batch_id
) def ON pd.id = def.batch_id

-- 1. CRITICAL DIMENSIONS
LEFT JOIN (
    SELECT 
        ith.batch_id,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'slope') > 0 THEN itr.sleeper_no END) AS rail_seat_slope,
        COUNT(DISTINCT CASE WHEN (INSTR(LOWER(COALESCE(ip.parameter_name,'')), 'insert') > 0 AND INSTR(LOWER(COALESCE(ip.parameter_name,'')), 'turn') > 0) OR INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'location of insert') > 0 THEN itr.sleeper_no END) AS location_of_inserts,
        COUNT(DISTINCT CASE WHEN (INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'rail seat') > 0) AND (INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'slope') = 0) THEN itr.sleeper_no END) AS rail_seat,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'toe gap') > 0 THEN itr.sleeper_no END) AS toe_gap,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'dowel') > 0 THEN itr.sleeper_no END) AS location_of_dowel,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'angularity') > 0 THEN itr.sleeper_no END) AS angularity_in_inserts,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'camber') > 0 THEN itr.sleeper_no END) AS camber_turnout
    FROM inspection_test_header ith
    JOIN inspection_test_result itr ON ith.id = itr.test_header_id
    LEFT JOIN inspection_parameter_result ipr ON itr.id = ipr.test_result_id
    LEFT JOIN inspection_reason_master irm ON ipr.reason_master_id = irm.id
    LEFT JOIN inspection_reason_master parent_irm ON irm.parent_reason_id = parent_irm.id
    LEFT JOIN inspection_parameter ip ON ipr.parameter_id = ip.id
    WHERE itr.module_id = 3 AND itr.result = 'REJECTED' AND itr.active = 1
    GROUP BY ith.batch_id
) crit ON pd.id = crit.batch_id

-- 2. VISUAL INSPECTION
LEFT JOIN (
    SELECT 
        ith.batch_id,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'surface') > 0 THEN itr.sleeper_no END) AS surface_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'honeycomb') > 0 THEN itr.sleeper_no END) AS honeycomb,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'crack') > 0 THEN itr.sleeper_no END) AS crack,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'insert') > 0 THEN itr.sleeper_no END) AS insert_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'dowel') > 0 THEN itr.sleeper_no END) AS dowel_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'wire') > 0 THEN itr.sleeper_no END) AS wire_slippage,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'ftc') > 0 THEN itr.sleeper_no END) AS ftc_defect
    FROM inspection_test_header ith
    JOIN inspection_test_result itr ON ith.id = itr.test_header_id
    LEFT JOIN inspection_parameter_result ipr ON itr.id = ipr.test_result_id
    LEFT JOIN inspection_reason_master irm ON ipr.reason_master_id = irm.id
    LEFT JOIN inspection_reason_master parent_irm ON irm.parent_reason_id = parent_irm.id
    LEFT JOIN inspection_parameter ip ON ipr.parameter_id = ip.id
    WHERE itr.module_id = 1 AND itr.result = 'REJECTED' AND itr.active = 1
    GROUP BY ith.batch_id
) vis ON pd.id = vis.batch_id

-- 3. NON-CRITICAL DIMENSIONS
LEFT JOIN (
    SELECT 
        ith.batch_id,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'outer gauge') > 0 THEN itr.sleeper_no END) AS outer_gauge,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'height') > 0 THEN itr.sleeper_no END) AS height_gauge,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'wind') > 0 THEN itr.sleeper_no END) AS wind_gauge,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'length') > 0 THEN itr.sleeper_no END) AS length_of_sleeper,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(ip.parameter_name,''),' ',COALESCE(irm.reason_name,''),' ',COALESCE(parent_irm.reason_name,''),' ',COALESCE(itr.rejection_reason,''))), 'width') > 0 THEN itr.sleeper_no END) AS width_of_sleeper
    FROM inspection_test_header ith
    JOIN inspection_test_result itr ON ith.id = itr.test_header_id
    LEFT JOIN inspection_parameter_result ipr ON itr.id = ipr.test_result_id
    LEFT JOIN inspection_reason_master irm ON ipr.reason_master_id = irm.id
    LEFT JOIN inspection_reason_master parent_irm ON irm.parent_reason_id = parent_irm.id
    LEFT JOIN inspection_parameter ip ON ipr.parameter_id = ip.id
    WHERE itr.module_id = 2 AND itr.result = 'REJECTED' AND itr.active = 1
    GROUP BY ith.batch_id
) dim ON pd.id = dim.batch_id

-- 4. DEMOULDING REASONS
LEFT JOIN (
    SELECT 
        di.batch_no,
        di.plant_id,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'surface') > 0 THEN dds.sleeper_no END) AS dem_surface_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'honeycomb') > 0 THEN dds.sleeper_no END) AS dem_honeycomb,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'crack') > 0 THEN dds.sleeper_no END) AS dem_crack,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'insert') > 0 THEN dds.sleeper_no END) AS dem_insert_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'dowel') > 0 THEN dds.sleeper_no END) AS dem_dowel_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'wire') > 0 THEN dds.sleeper_no END) AS dem_wire_slippage,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'outer gauge') > 0 THEN dds.sleeper_no END) AS dem_outer_gauge,
        COUNT(DISTINCT CASE WHEN (INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'rail seat') > 0) AND (INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'slope') = 0) THEN dds.sleeper_no END) AS dem_rail_seat,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'toe gap') > 0 THEN dds.sleeper_no END) AS dem_toe_gap,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'slope') > 0 THEN dds.sleeper_no END) AS dem_slope,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'height') > 0 THEN dds.sleeper_no END) AS dem_height_gauge,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(CONCAT(COALESCE(dds.visual_reason,''),' ',COALESCE(dds.dim_reason,''))), 'length') > 0 THEN dds.sleeper_no END) AS dem_length_sleeper,
        COUNT(DISTINCT CASE WHEN TRIM(COALESCE(dds.visual_reason, '')) <> '' OR TRIM(COALESCE(dds.dim_reason, '')) <> '' THEN dds.sleeper_no END) AS total_demoulding_rejected
    FROM demoulding_defective_sleepers dds
    JOIN demoulding_inspection di ON dds.inspection_id = di.id
    WHERE dds.sleeper_no IS NOT NULL 
      AND TRIM(dds.sleeper_no) <> ''
      AND (
          TRIM(COALESCE(dds.visual_reason, '')) <> '' 
          OR 
          TRIM(COALESCE(dds.dim_reason, '')) <> ''
      )
    GROUP BY di.batch_no, di.plant_id
) dem ON CONVERT(pd.batch_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(dem.batch_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
     AND (
         CONVERT(pd.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(dem.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
         OR CONVERT(REPLACE(pd.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(dem.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
     )

-- 5. MAIN IE REASONS
LEFT JOIN (
    SELECT 
        ibs.batch_no,
        ibs.plant_id,
        ibs.call_no,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'broken') > 0 OR INSTR(LOWER(fcrs.reason), 'damage') > 0 OR INSTR(LOWER(fcrs.reason), 'surface') > 0 THEN fcrs.sleeper_code END) AS mie_surface_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'honeycom') > 0 THEN fcrs.sleeper_code END) AS mie_honeycomb,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'crack') > 0 THEN fcrs.sleeper_code END) AS mie_crack,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'insert') > 0 AND INSTR(LOWER(fcrs.reason), 'turn') = 0 THEN fcrs.sleeper_code END) AS mie_insert_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'dowel') > 0 THEN fcrs.sleeper_code END) AS mie_dowel_defect,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'wire') > 0 THEN fcrs.sleeper_code END) AS mie_wire_slippage,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'dimension') > 0 OR INSTR(LOWER(fcrs.reason), 'gauge') > 0 THEN fcrs.sleeper_code END) AS mie_outer_gauge,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'slope') > 0 THEN fcrs.sleeper_code END) AS mie_slope,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'toe gap') > 0 THEN fcrs.sleeper_code END) AS mie_toe_gap,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'location of insert') > 0 OR (INSTR(LOWER(fcrs.reason), 'insert') > 0 AND INSTR(LOWER(fcrs.reason), 'turn') > 0) THEN fcrs.sleeper_code END) AS mie_inserts_turnout,
        COUNT(DISTINCT CASE WHEN INSTR(LOWER(fcrs.reason), 'angularity') > 0 THEN fcrs.sleeper_code END) AS mie_angularity,
        COUNT(DISTINCT CASE WHEN fcrs.reason IS NOT NULL AND TRIM(fcrs.reason) <> '' AND fcrs.reason = 'Rejected' THEN fcrs.sleeper_code END) AS mie_general_reject,
        COUNT(DISTINCT CASE WHEN fcrs.reason IS NOT NULL AND TRIM(fcrs.reason) <> '' THEN fcrs.sleeper_code END) AS total_main_ie_rejected
    FROM final_call_rejected_sleepers fcrs
    JOIN ie_batch_summary ibs ON fcrs.batch_id = ibs.id
    WHERE fcrs.reason IS NOT NULL AND TRIM(fcrs.reason) <> ''
    GROUP BY ibs.batch_no, ibs.plant_id, ibs.call_no
) mie ON CONVERT(pd.batch_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(mie.batch_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
    AND CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(mie.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
    AND (
        CONVERT(pd.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(mie.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        OR CONVERT(REPLACE(pd.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(mie.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
    )

WHERE cs.IC_NUMBER = 'E/SF-091426005/RP3'
   OR sic.call_no = 'SF-091426005'

ORDER BY `Batch Offered`;
