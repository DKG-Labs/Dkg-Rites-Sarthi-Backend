package com.sarthi.Sms.entity.visualInspection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="visual_inspection_duty")
public class ViDutyEntity {
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name="duty_id")
    private String dutyId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "shift", nullable = false, length = 10)
    private String shift;

    @Column(name = "mill", nullable = false, length = 10)
    private String mill;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "rail_grade", nullable = false, length = 50)
    private String railGrade;

    @Column(name = "rail_section", nullable = false, length = 50)
    private String railSection;

    @Column(name = "standard_offered_length", nullable = false)
    private BigDecimal stdOffLength;

    @Column(name = "ie_name_1", nullable = false, length = 255)
    private String ieName1;

    @Column(name = "ie_name_2", length = 255)
    private String ieName2;

    @Column(name = "ie_name_3", length = 255)
    private String ieName3;

    @Column(name = "rcl_ie_name_1", nullable = false, length = 255)
    private String rclIeName1;

    @Column(name = "rcl_ie_name_2", nullable = false, length = 255)
    private String rclIeName2;

    @Column(name = "rcl_ie_name_3", length = 255)
    private String rclIeName3;

    @Column(name = "ie_confirmation")
    private Boolean ieConfirmation;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "shift_remarks", length = 100)
    private String shiftRemarks;
}
