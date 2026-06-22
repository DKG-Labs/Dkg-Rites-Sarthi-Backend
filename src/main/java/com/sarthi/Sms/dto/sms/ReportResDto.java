package com.sarthi.Sms.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResDto {
    private Date date;
    private String smsNumber;
    private String shift;
    private String casterNumber;
    private String railGrade;
    private Long numberOfHeatsCast;
    private Long numberOfHeatsRejected;
    private Long numberOfDivertedHeats;
    private String rejectedHeatNumbers;
    private BigDecimal weightOfHeatsCast;
    private BigDecimal weightOfPrimeBlooms;
    private BigDecimal weightOfCOBlooms;
    private BigDecimal weightOfAcceptedBlooms;
    private BigDecimal weightOfRejectedBlooms;
    private String reasonForRejection;

    // Add formatted date and shift field for Excel export (similar to Heat Summary)
    private String dateAndShift;

    // Constructor without dateAndShift (for backward compatibility)
    public ReportResDto(Date date, String smsNumber, String shift, String casterNumber, String railGrade,
                       Long numberOfHeatsCast, Long numberOfHeatsRejected, Long numberOfDivertedHeats,
                       String rejectedHeatNumbers, BigDecimal weightOfHeatsCast, BigDecimal weightOfPrimeBlooms,
                       BigDecimal weightOfCOBlooms, BigDecimal weightOfAcceptedBlooms, BigDecimal weightOfRejectedBlooms,
                       String reasonForRejection) {
        this.date = date;
        this.smsNumber = smsNumber;
        this.shift = shift;
        this.casterNumber = casterNumber;
        this.railGrade = railGrade;
        this.numberOfHeatsCast = numberOfHeatsCast;
        this.numberOfHeatsRejected = numberOfHeatsRejected;
        this.numberOfDivertedHeats = numberOfDivertedHeats;
        this.rejectedHeatNumbers = rejectedHeatNumbers;
        this.weightOfHeatsCast = weightOfHeatsCast;
        this.weightOfPrimeBlooms = weightOfPrimeBlooms;
        this.weightOfCOBlooms = weightOfCOBlooms;
        this.weightOfAcceptedBlooms = weightOfAcceptedBlooms;
        this.weightOfRejectedBlooms = weightOfRejectedBlooms;
        this.reasonForRejection = reasonForRejection;
    }
}

