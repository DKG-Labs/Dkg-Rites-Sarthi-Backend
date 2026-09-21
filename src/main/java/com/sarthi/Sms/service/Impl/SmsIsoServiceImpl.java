package com.sarthi.Sms.service.Impl;

import com.sarthi.Sms.dto.sms.iso.ChemicalAnalysisIsoResDto;
import com.sarthi.Sms.dto.sms.iso.IsoReportReqDto;
import com.sarthi.Sms.dto.sms.iso.VerificationIsoResDto;
import com.sarthi.Sms.service.SmsIsoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SmsIsoServiceImpl implements SmsIsoService {

    @PersistenceContext
    private EntityManager entityManager;

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();
        try {
            if (dateStr.contains("/")) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (dateStr.contains("-")) {
                if (dateStr.indexOf('-') == 4) {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<VerificationIsoResDto> getVerificationIsoDtls(IsoReportReqDto req) {
        LocalDate parsedDate = parseDate(req.getDate());
        String sms = req.getSms();
        String shift = req.getShift();
        String railGrade = req.getRailGrade();

        boolean isSms3 = "SMS 3".equalsIgnoreCase(sms) || "SMS3".equalsIgnoreCase(sms);
        String dutyHeatTable = isSms3 ? "duty_heat_sms3" : "duty_heat_sms2";
        String heatDetailTable = isSms3 ? "heat_detail_sms3" : "heat_detail_sms2";
        String bloomDetailTable = isSms3 ? "bloom_detail_sms3" : "bloom_detail_sms2";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("shd.heat_number, ")
           .append("shd.heat_stage, ")
           .append("shd.turn_down_temp, ")
           .append("shd.turn_down_temp_wv, ")
           .append("shd.degassing_vacuum, ")
           .append("shd.degassing_vacuum_wv, ")
           .append("shd.degassing_duration, ")
           .append("shd.degassing_duration_wv, ")
           .append("shd.casting_temp, ")
           .append("shd.casting_temp_2, ")
           .append("COALESCE(bd.number_of_co_blooms, shd.number_of_co_blooms, 0) as number_of_co_blooms, ")
           .append("sd.is_ladle_to_tundish_used, ")
           .append("sd.is_tundish_to_mould_used ")
           .append("FROM sms_duty sd ")
           .append("JOIN ").append(dutyHeatTable).append(" dh ON sd.duty_id = dh.duty_id ")
           .append("JOIN ").append(heatDetailTable).append(" shd ON dh.heat_number = shd.heat_number ")
           .append("LEFT JOIN ").append(bloomDetailTable).append(" bd ON shd.heat_number = bd.cast_number ")
           .append("WHERE 1=1 ");

        if (parsedDate != null) {
            sql.append("AND sd.date = :date ");
        }
        if (shift != null && !shift.trim().isEmpty()) {
            sql.append("AND sd.shift = :shift ");
        }
        if (railGrade != null && !railGrade.trim().isEmpty()) {
            sql.append("AND sd.rail_grade = :railGrade ");
        }
        if (sms != null && !sms.trim().isEmpty()) {
            sql.append("AND sd.sms = :sms ");
        }

        sql.append("ORDER BY shd.heat_number");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (parsedDate != null) {
            query.setParameter("date", parsedDate);
        }
        if (shift != null && !shift.trim().isEmpty()) {
            query.setParameter("shift", shift.trim());
        }
        if (railGrade != null && !railGrade.trim().isEmpty()) {
            query.setParameter("railGrade", railGrade.trim());
        }
        if (sms != null && !sms.trim().isEmpty()) {
            query.setParameter("sms", sms.trim());
        }

        List<Object[]> rows = query.getResultList();
        List<VerificationIsoResDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            VerificationIsoResDto dto = new VerificationIsoResDto();
            dto.setHeatNumber(row[0] != null ? row[0].toString() : null);
            dto.setHeatStage(row[1] != null ? row[1].toString() : null);
            dto.setTurnDownTemp(row[2] != null ? ((Number) row[2]).intValue() : null);
            dto.setTurnDownTempWv(row[3] != null ? row[3].toString() : null);
            dto.setDegassingVacuum(row[4] != null ? (row[4] instanceof BigDecimal ? (BigDecimal) row[4] : new BigDecimal(row[4].toString())) : null);
            dto.setDegassingVacuumWv(row[5] != null ? row[5].toString() : null);
            dto.setDegassingDuration(row[6] != null ? ((Number) row[6]).intValue() : null);
            dto.setDegassingDurationWv(row[7] != null ? row[7].toString() : null);
            dto.setCastingTemp(row[8] != null ? ((Number) row[8]).intValue() : null);
            dto.setCastingTemp2(row[9] != null ? ((Number) row[9]).intValue() : null);
            dto.setNumberOfCoBlooms(row[10] != null ? ((Number) row[10]).intValue() : null);
            dto.setIsLadleToTundishUsed(row[11] != null && ("1".equals(row[11].toString()) || "true".equalsIgnoreCase(row[11].toString())));
            dto.setIsTundishToMouldUsed(row[12] != null && ("1".equals(row[12].toString()) || "true".equalsIgnoreCase(row[12].toString())));
            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ChemicalAnalysisIsoResDto> getChemicalAnalysisIsoDtls(IsoReportReqDto req) {
        LocalDate parsedDate = parseDate(req.getDate());
        String sms = req.getSms();
        String shift = req.getShift();
        String railGrade = req.getRailGrade();

        boolean isSms3 = "SMS 3".equalsIgnoreCase(sms) || "SMS3".equalsIgnoreCase(sms);
        String dutyHeatTable = isSms3 ? "duty_heat_sms3" : "duty_heat_sms2";
        String heatDetailTable = isSms3 ? "heat_detail_sms3" : "heat_detail_sms2";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("shd.heat_number, ")
           .append("shd.sequence_number, ")
           .append("shd.nitrogen, ")
           .append("shd.oxygen, ")
           .append("shd.hydris, ")
           .append("shd.degassing_vacuum, ")
           .append("shd.degassing_duration, ")
           .append("shd.heat_remark ")
           .append("FROM sms_duty sd ")
           .append("JOIN ").append(dutyHeatTable).append(" dh ON sd.duty_id = dh.duty_id ")
           .append("JOIN ").append(heatDetailTable).append(" shd ON dh.heat_number = shd.heat_number ")
           .append("WHERE 1=1 ");

        if (parsedDate != null) {
            sql.append("AND sd.date = :date ");
        }
        if (shift != null && !shift.trim().isEmpty()) {
            sql.append("AND sd.shift = :shift ");
        }
        if (railGrade != null && !railGrade.trim().isEmpty()) {
            sql.append("AND sd.rail_grade = :railGrade ");
        }
        if (sms != null && !sms.trim().isEmpty()) {
            sql.append("AND sd.sms = :sms ");
        }

        sql.append("ORDER BY shd.heat_number");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (parsedDate != null) {
            query.setParameter("date", parsedDate);
        }
        if (shift != null && !shift.trim().isEmpty()) {
            query.setParameter("shift", shift.trim());
        }
        if (railGrade != null && !railGrade.trim().isEmpty()) {
            query.setParameter("railGrade", railGrade.trim());
        }
        if (sms != null && !sms.trim().isEmpty()) {
            query.setParameter("sms", sms.trim());
        }

        List<Object[]> rows = query.getResultList();
        List<ChemicalAnalysisIsoResDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            ChemicalAnalysisIsoResDto dto = new ChemicalAnalysisIsoResDto();
            dto.setHeatNumber(row[0] != null ? row[0].toString() : null);
            dto.setSequenceNumber(row[1] != null ? row[1].toString() : null);
            dto.setNitrogen(row[2] != null ? (row[2] instanceof BigDecimal ? (BigDecimal) row[2] : new BigDecimal(row[2].toString())) : null);
            dto.setOxygen(row[3] != null ? (row[3] instanceof BigDecimal ? (BigDecimal) row[3] : new BigDecimal(row[3].toString())) : null);
            dto.setHydris(row[4] != null ? (row[4] instanceof BigDecimal ? (BigDecimal) row[4] : new BigDecimal(row[4].toString())) : null);
            dto.setDegassingVacuum(row[5] != null ? (row[5] instanceof BigDecimal ? (BigDecimal) row[5] : new BigDecimal(row[5].toString())) : null);
            dto.setDegassingDuration(row[6] != null ? ((Number) row[6]).intValue() : null);
            dto.setHeatRemark(row[7] != null ? row[7].toString() : null);
            result.add(dto);
        }

        return result;
    }
}
