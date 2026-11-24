package com.sarthi.Sms.entity.heatnumber;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "heatnumber_inspection")
public class HeatnumberInspectionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "rail_id", nullable = false, unique = true, length = 50)
    private String railId;
    
    @Column(name = "shift", length = 5)
    private String shift;
    
    @Column(name = "heatnumber", length = 15)
    private String heatnumber;
    
    @Column(name = "ocr_hn", length = 15)
    private String ocrHn;
    
    @Column(name = "ai_status")
    private Boolean aiStatus = true;
    
    @Column(name = "admin_status")
    private Boolean adminStatus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public HeatnumberInspectionEntity() {}
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getRailId() {
        return railId;
    }
    
    public void setRailId(String railId) {
        this.railId = railId;
    }
    
    public String getShift() {
        return shift;
    }
    
    public void setShift(String shift) {
        this.shift = shift;
    }
    
    public String getHeatnumber() {
        return heatnumber;
    }
    
    public void setHeatnumber(String heatnumber) {
        this.heatnumber = heatnumber;
    }
    
    public String getOcrHn() {
        return ocrHn;
    }
    
    public void setOcrHn(String ocrHn) {
        this.ocrHn = ocrHn;
    }
    
    public Boolean getAiStatus() {
        return aiStatus;
    }
    
    public void setAiStatus(Boolean aiStatus) {
        this.aiStatus = aiStatus;
    }
    
    public Boolean getAdminStatus() {
        return adminStatus;
    }
    
    public void setAdminStatus(Boolean adminStatus) {
        this.adminStatus = adminStatus;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
