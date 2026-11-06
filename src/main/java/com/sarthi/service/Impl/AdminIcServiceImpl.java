package com.sarthi.service.Impl;

import com.sarthi.SRailPad.repository.inspectionCall.RailpadFinalIcEditRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadProcessIcEditRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperFinalIcEditRepository;
import com.sarthi.repository.finalmaterial.FinalIcEditRepository;
import com.sarthi.repository.processmaterial.ProcessIcEditRepository;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import com.sarthi.service.AdminIcService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class AdminIcServiceImpl implements AdminIcService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private RmIcEditRepository rmIcEditRepository;
    private ProcessIcEditRepository processIcEditRepository;
    private FinalIcEditRepository finalIcEditRepository;
    private SleeperFinalIcEditRepository sleeperFinalIcEditRepository;
    private RailpadProcessIcEditRepository railpadProcessIcEditRepository;
    private RailpadFinalIcEditRepository railpadFinalIcEditRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getIcDetails(String callNo) {
        String normalized = callNo.trim().toUpperCase();
        Object[] row = findIcRow(prefixOf(normalized), normalized);
        if (row == null) {
            throw new IllegalArgumentException("No IC found for call number: " + normalized);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("callNo", normalized);
        result.put("icNumber", row[0] != null ? row[0].toString() : normalized);
        result.put("currentDate", toLocalDate(row[1]) != null ? toLocalDate(row[1]).toString() : null);
        result.put("icLink", "/api/certificate-storage/view/" + normalized + ".pdf");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateIcDate(String callNo, LocalDate newDate) {
        if (newDate.isAfter(LocalDate.now(IST))) {
            throw new IllegalArgumentException("Cannot set a future date. Date must be today or earlier.");
        }

        String normalized = callNo.trim().toUpperCase();
        String prefix = prefixOf(normalized);
        Object[] before = findIcRow(prefix, normalized);
        if (before == null) {
            throw new IllegalArgumentException("No IC found for call number: " + normalized);
        }

        // Noon, not midnight: the JDBC IST->UTC shift (-5:30) would push midnight into the previous day
        LocalDateTime newDateTime = newDate.atTime(12, 0);
        int updated = switch (prefix) {
            case "ER" -> rmIcEditRepository.updateCreatedAtByCallNo(normalized, newDateTime);
            case "EP" -> processIcEditRepository.updateCreatedAtByCallNo(normalized, newDateTime);
            case "EF" -> finalIcEditRepository.updateCreatedAtByCallNo(normalized, newDateTime);
            case "SF" -> sleeperFinalIcEditRepository.updateCreatedAtByCallNo(normalized, newDateTime);
            case "RPP" -> railpadProcessIcEditRepository.updateCreatedAtByCallNo(normalized, newDateTime);
            case "RPF" -> railpadFinalIcEditRepository.updateCreatedAtByCallNo(normalized, newDateTime);
            default -> throw new IllegalArgumentException("Unsupported call number: " + normalized);
        };

        LocalDate oldDate = toLocalDate(before[1]);
        log.info("[ADMIN-IC-DATE] callNo={} icNumber={} oldDate={} newDate={} rows={}",
                normalized, before[0], oldDate, newDate, updated);

        Map<String, Object> result = new HashMap<>();
        result.put("callNo", normalized);
        result.put("icNumber", before[0] != null ? before[0].toString() : normalized);
        result.put("oldDate", oldDate != null ? oldDate.toString() : null);
        result.put("newDate", newDate.toString());
        result.put("recordsUpdated", updated);
        return result;
    }

    private Object[] findIcRow(String prefix, String callNo) {
        List<Object[]> rows = switch (prefix) {
            case "ER" -> rmIcEditRepository.findIcNumberAndCreatedAtByCallNo(callNo);
            case "EP" -> processIcEditRepository.findIcNumberAndCreatedAtByCallNo(callNo);
            case "EF" -> finalIcEditRepository.findIcNumberAndCreatedAtByCallNo(callNo);
            case "SF" -> sleeperFinalIcEditRepository.findIcNumberAndCreatedAtByCallNo(callNo);
            case "RPP" -> railpadProcessIcEditRepository.findIcNumberAndCreatedAtByCallNo(callNo);
            case "RPF" -> railpadFinalIcEditRepository.findIcNumberAndCreatedAtByCallNo(callNo);
            default -> throw new IllegalArgumentException("Unsupported call number: " + callNo);
        };
        return (rows == null || rows.isEmpty()) ? null : rows.get(0);
    }

    private String prefixOf(String callNo) {
        if (callNo.isEmpty()) {
            throw new IllegalArgumentException("Call number cannot be empty");
        }
        // RPP/RPF must be checked before shorter prefixes
        for (String p : new String[]{"RPP", "RPF", "ER", "EP", "EF", "SF"}) {
            if (callNo.startsWith(p + "-")) {
                return p;
            }
        }
        throw new IllegalArgumentException(
                "Unsupported call number: " + callNo + ". Expected ER-, EP-, EF-, SF-, RPP- or RPF-");
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (value instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (value instanceof java.sql.Date d) return d.toLocalDate();
        if (value instanceof LocalDate ld) return ld;
        return null;
    }
}
