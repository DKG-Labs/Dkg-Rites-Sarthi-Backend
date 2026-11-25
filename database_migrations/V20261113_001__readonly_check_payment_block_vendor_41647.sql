-- READ-ONLY: why is vendor 41647 blocked from raising calls ("Payment Pending")?
-- The block comes from these two tables only (WorkflowServiceImpl.getCancelledCallsForPayment).

-- 0. Who does call ER-26102901 really belong to?
SELECT ic.ic_number, ic.vendor_id, ic.po_no, ic.place_of_inspection FROM inspection_calls ic WHERE ic.ic_number = 'ER-26102901';
SELECT call_number, vendor_code, cancellation_basis, final_cancellation_charges FROM call_cancellation_details WHERE call_number = 'ER-26102901';
SELECT call_number, vendor_code, liability_type, amount, payment_status FROM vendor_financial_liabilities WHERE call_number = 'ER-26102901';

-- 1. Cancellation rows saved for this vendor
SELECT ccd.call_number, ccd.vendor_code, ccd.cancellation_basis,
       ccd.calculated_charges, ccd.final_cancellation_charges, ccd.reasons, ccd.cancellation_description
FROM call_cancellation_details ccd
WHERE ccd.vendor_code = '41647';

-- 2. Liability rows for this vendor (ANY liability_type is counted, not just cancellation)
SELECT vfl.call_number, vfl.vendor_code, vfl.liability_type, vfl.amount, vfl.payment_status
FROM vendor_financial_liabilities vfl
WHERE vfl.vendor_code = '41647';

-- 3. Current state of those calls (is the call really cancelled?)
SELECT ic.ic_number, ic.status, ic.po_no, ic.place_of_inspection
FROM inspection_calls ic
WHERE ic.ic_number IN (
    SELECT call_number FROM call_cancellation_details WHERE vendor_code = '41647'
    UNION
    SELECT call_number FROM vendor_financial_liabilities WHERE vendor_code = '41647'
);

-- 4. Latest workflow remarks for those calls (remarks containing "CHARGEABLE" also trigger the block)
SELECT wt.request_id, wt.action, wt.status, wt.remarks, wt.created_date
FROM workflow_transition wt
WHERE wt.workflow_transition_id IN (
    SELECT MAX(workflow_transition_id) FROM workflow_transition
    WHERE request_id IN (
        SELECT call_number FROM call_cancellation_details WHERE vendor_code = '41647'
        UNION
        SELECT call_number FROM vendor_financial_liabilities WHERE vendor_code = '41647'
    )
    GROUP BY request_id
);
