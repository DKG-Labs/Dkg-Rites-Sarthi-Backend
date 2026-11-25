-- READ-ONLY. What the vendor offered in the partial diversion for EP-26110302 vs source IC quantities.
SELECT r.request_no, r.diversion_type, r.source_call_no, r.source_ic_no, r.total_diversion_qty,
       i.heat_no, i.lot_no, i.accepted_qty, i.downstream_consumed_qty, i.available_balance_qty,
       i.diversion_qty, i.remaining_balance_qty
FROM erc_material_diversion_request r
JOIN erc_material_diversion_item i ON i.diversion_request_id = r.id
WHERE r.request_no IN (SELECT diversion_request_no FROM inspection_calls WHERE ic_number = 'EP-26110302')
   OR r.request_no = 'EP-26110302';

-- Source/own process results the IBS payload sums (check for duplicate rows per heat/line)
SELECT inspection_call_no, id, total_manufactured, total_accepted, total_rejected
FROM process_line_final_result
WHERE inspection_call_no IN ('EP-26110302',
      (SELECT source_call_no FROM erc_material_diversion_request
        WHERE request_no = (SELECT diversion_request_no FROM inspection_calls WHERE ic_number = 'EP-26110302')));
