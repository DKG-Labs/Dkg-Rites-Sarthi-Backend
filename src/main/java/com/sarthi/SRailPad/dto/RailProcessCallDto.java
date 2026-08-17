package com.sarthi.SRailPad.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RailProcessCallDto {
    private String callNo;
    private String poNo;
    private String poSr;
    private String vendorCode;
    private String plantId;
    private String railPadType;
    private Integer totalQty;
    private String status;
    private LocalDateTime createdAt;

    // Process Call Details
    private String drawingNo;
    private String uom;
    private Integer qtyOnOrder;
    private Integer qtyAcceptedTillNow;
    private Integer qtyDesiredForFinal;
    private Integer qtyDue;
    private LocalDate productionInitiationDate;

    public String getCallNo() { return callNo; }
    public void setCallNo(String callNo) { this.callNo = callNo; }

    public String getPoNo() { return poNo; }
    public void setPoNo(String poNo) { this.poNo = poNo; }

    public String getPoSr() { return poSr; }
    public void setPoSr(String poSr) { this.poSr = poSr; }

    public String getVendorCode() { return vendorCode; }
    public void setVendorCode(String vendorCode) { this.vendorCode = vendorCode; }

    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public String getRailPadType() { return railPadType; }
    public void setRailPadType(String railPadType) { this.railPadType = railPadType; }

    public Integer getTotalQty() { return totalQty; }
    public void setTotalQty(Integer totalQty) { this.totalQty = totalQty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDrawingNo() { return drawingNo; }
    public void setDrawingNo(String drawingNo) { this.drawingNo = drawingNo; }

    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }

    public Integer getQtyOnOrder() { return qtyOnOrder; }
    public void setQtyOnOrder(Integer qtyOnOrder) { this.qtyOnOrder = qtyOnOrder; }

    public Integer getQtyAcceptedTillNow() { return qtyAcceptedTillNow; }
    public void setQtyAcceptedTillNow(Integer qtyAcceptedTillNow) { this.qtyAcceptedTillNow = qtyAcceptedTillNow; }

    public Integer getQtyDesiredForFinal() { return qtyDesiredForFinal; }
    public void setQtyDesiredForFinal(Integer qtyDesiredForFinal) { this.qtyDesiredForFinal = qtyDesiredForFinal; }

    public Integer getQtyDue() { return qtyDue; }
    public void setQtyDue(Integer qtyDue) { this.qtyDue = qtyDue; }

    public LocalDate getProductionInitiationDate() { return productionInitiationDate; }
    public void setProductionInitiationDate(LocalDate productionInitiationDate) { this.productionInitiationDate = productionInitiationDate; }
}
