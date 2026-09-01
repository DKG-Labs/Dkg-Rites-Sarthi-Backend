package com.sarthi.SRailPad.dto;

import lombok.Data;

/**
 * Railpad-specific DTO for the Inspection Initiation Summary screen.
 * This is completely isolated from the shared Sleeper portal
 * PoDataForSectionsDto.
 *
 * Data sources:
 * Section A -> po_header table
 * Section B -> po_item table (matched by item_sr_no)
 * Section C -> rail_inspection_call table
 */
@Data
public class RailPoSummaryDto {

    // ---- Section A: Main PO Information (po_header) ----

    /** RLY/PO_NO format: SER/60260074102063 */
    private String rlyPoNo;

    /** Railway short name e.g. SER */
    private String rlyShortName;

    /** Raw PO number e.g. 60260074102063 */
    private String poNo;

    /** PO Date in dd/MM/yyyy format */
    private String poDate;

    /** Total PO Quantity (sum of all po_item.qty) */
    private Integer poQty;

    /** Vendor name extracted from vendorDetails / firmDetails */
    private String vendorName;

    /** SCR Code / Railway short name e.g. SER, SCR */
    private String scrCode;

    /** Vendor code e.g. :1007406 */
    private String vendorCode;

    /** MA Number (from po_ma_header, N/A if none) */
    private String maNo;

    /** MA Date in dd/MM/yyyy format (N/A if none) */
    private String maDate;

    /** Purchasing authority e.g. APURBA SAHA~SMM/P/G */
    private String purchasingAuthority;

    /** Bill paying officer e.g. FA&CAO(S)/GRC */
    private String billPayingOfficer;

    // ---- Section B: Inspection Call Details (po_item + rail_inspection_call) ----

    /** Full serial: SER/60260074102063/001 */
    private String rlyPoNoSerial;

    /** Item description from po_item */
    private String itemDesc;

    /** PO serial number e.g. 001 */
    private String poSerialNo;

    /**
     * Quantity for this specific PO serial number (from po_item.qty where
     * item_sr_no matches)
     */
    private Integer poSrQty;

    /** Value of this specific PO serial number (from po_item.value or basicValue) */
    private java.math.BigDecimal poSrValue;

    /** Rate of this specific PO serial number (from po_item.rate) */
    private java.math.BigDecimal rate;

    /** Total PO Value */
    private String poValue;

    /** UOM from po_item.uom e.g. Nos. */
    private String unit;

    /** Consignee name from po_item.imms_consignee_name */
    private String consignee;

    /** Original Delivery Period from po_item.delivery_date */
    private String origDp;

    /** Extended Delivery Period from po_item.extended_delivery_date */
    private String extDp;

    /** Place of inspection derived from firmDetails/vendorDetails */
    private String placeOfInspection;

    /** ERC type from rail_inspection_call.rail_pad_type */
    private String ercType;

    /** Total quantity offered in this call from rail_inspection_call.total_qty */
    private Integer totalOfferedQty;

    /**
     * Drawing number from rail_process_call_details.drawing_no (set during vendor
     * call raising)
     */
    private String drawingNo;

    /** RITES RIO code from rail_workflow_transaction.rio e.g. NRIO, SRIO, ERIO, WRIO, CRIO */
    private String rio;

    /** Offered Installment Number (count of inspection calls raised for this PO) */
    private String offeredInstallmentNo;

    public String getRlyPoNo() { return rlyPoNo; }
    public void setRlyPoNo(String rlyPoNo) { this.rlyPoNo = rlyPoNo; }

    public String getRlyShortName() { return rlyShortName; }
    public void setRlyShortName(String rlyShortName) { this.rlyShortName = rlyShortName; }

    public String getPoNo() { return poNo; }
    public void setPoNo(String poNo) { this.poNo = poNo; }

    public String getPoDate() { return poDate; }
    public void setPoDate(String poDate) { this.poDate = poDate; }

    public Integer getPoQty() { return poQty; }
    public void setPoQty(Integer poQty) { this.poQty = poQty; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getScrCode() { return scrCode; }
    public void setScrCode(String scrCode) { this.scrCode = scrCode; }

    public String getVendorCode() { return vendorCode; }
    public void setVendorCode(String vendorCode) { this.vendorCode = vendorCode; }

    public String getMaNo() { return maNo; }
    public void setMaNo(String maNo) { this.maNo = maNo; }

    public String getMaDate() { return maDate; }
    public void setMaDate(String maDate) { this.maDate = maDate; }

    public String getPurchasingAuthority() { return purchasingAuthority; }
    public void setPurchasingAuthority(String purchasingAuthority) { this.purchasingAuthority = purchasingAuthority; }

    public String getBillPayingOfficer() { return billPayingOfficer; }
    public void setBillPayingOfficer(String billPayingOfficer) { this.billPayingOfficer = billPayingOfficer; }

    public String getRlyPoNoSerial() { return rlyPoNoSerial; }
    public void setRlyPoNoSerial(String rlyPoNoSerial) { this.rlyPoNoSerial = rlyPoNoSerial; }

    public String getItemDesc() { return itemDesc; }
    public void setItemDesc(String itemDesc) { this.itemDesc = itemDesc; }

    public String getPoSerialNo() { return poSerialNo; }
    public void setPoSerialNo(String poSerialNo) { this.poSerialNo = poSerialNo; }

    public Integer getPoSrQty() { return poSrQty; }
    public void setPoSrQty(Integer poSrQty) { this.poSrQty = poSrQty; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getConsignee() { return consignee; }
    public void setConsignee(String consignee) { this.consignee = consignee; }

    public String getOrigDp() { return origDp; }
    public void setOrigDp(String origDp) { this.origDp = origDp; }

    public String getExtDp() { return extDp; }
    public void setExtDp(String extDp) { this.extDp = extDp; }

    public String getPlaceOfInspection() { return placeOfInspection; }
    public void setPlaceOfInspection(String placeOfInspection) { this.placeOfInspection = placeOfInspection; }

    public String getErcType() { return ercType; }
    public void setErcType(String ercType) { this.ercType = ercType; }

    public Integer getTotalOfferedQty() { return totalOfferedQty; }
    public void setTotalOfferedQty(Integer totalOfferedQty) { this.totalOfferedQty = totalOfferedQty; }

    public String getDrawingNo() { return drawingNo; }
    public void setDrawingNo(String drawingNo) { this.drawingNo = drawingNo; }

    /** Case Number from po_header */
    private String caseNo;

    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String caseNo) { this.caseNo = caseNo; }

    /** Remarks from rail_inspection_call */
    private String remarks;

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getRio() { return rio; }
    public void setRio(String rio) { this.rio = rio; }

    public String getOfferedInstallmentNo() { return offeredInstallmentNo; }
    public void setOfferedInstallmentNo(String offeredInstallmentNo) { this.offeredInstallmentNo = offeredInstallmentNo; }
}
