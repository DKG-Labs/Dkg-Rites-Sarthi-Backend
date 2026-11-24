package com.sarthi.Sms.entity.visualInspection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name="visual_inspection_defect_data")
public class ViDefectDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="defect_data_id", nullable = false)
    private Integer defectDataId;

    @Column(name = "defect_category", nullable = false, length = 50)
    private String defectCategory;

    @Column(name = "defect_type", nullable = false, length = 50)
    private String defectType;

    @Column(name = "location", nullable = false, length = 50)
    private String location;

    @Column(name = "position", nullable = false, length = 50)
    private String position;

    //  @Column(name = "heat_number", nullable = false)
    // private String heatNo;

    @Column(name = "rail_id", nullable = false)
    private String railId;

    // Manual getters and setters to avoid Lombok issues
    public Integer getDefectDataId() {
        return defectDataId;
    }

    public void setDefectDataId(Integer defectDataId) {
        this.defectDataId = defectDataId;
    }

    public String getDefectCategory() {
        return defectCategory;
    }

    public void setDefectCategory(String defectCategory) {
        this.defectCategory = defectCategory;
    }

    public String getDefectType() {
        return defectType;
    }

    public void setDefectType(String defectType) {
        this.defectType = defectType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRailId() {
        return railId;
    }

    public void setRailId(String railId) {
        this.railId = railId;
    }
}
