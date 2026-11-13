package com.sarthi.SRailPad.service.Impl.dashboard;

import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.dto.reports.InspectionCallsReportDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Rail Pad calls for the Controlling Manager dashboard. Same row shape and date handling as the
 * ERC (/api/reports/cm-*) and Sleeper (/api/sleeper-dashboard/cm-*) reports so the CM page can merge them.
 */
@Service
public class RailCmDashboardService {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RailInspectionCallRepository railInspectionCallRepository;

    private final com.sarthi.service.CmCallScopeService cmCallScopeService;

    public RailCmDashboardService(RailInspectionCallRepository railInspectionCallRepository,
                                  com.sarthi.service.CmCallScopeService cmCallScopeService) {
        this.railInspectionCallRepository = railInspectionCallRepository;
        this.cmCallScopeService = cmCallScopeService;
    }

    @Transactional(readOnly = true)
    public List<InspectionCallsReportDto> getCmInspectionCalls(String startDate, String endDate) {
        LocalDateTime[] range = range(startDate, endDate);
        return map(railInspectionCallRepository.getRailCmInspectionCalls(range[0], range[1], cmCallScopeService.cmUserIdFilter()));
    }

    @Transactional(readOnly = true)
    public List<InspectionCallsReportDto> getCmOverdueCalls(String startDate, String endDate) {
        LocalDateTime[] range = range(startDate, endDate);
        return map(railInspectionCallRepository.getRailCmOverdueCalls(range[0], range[1], cmCallScopeService.cmUserIdFilter()));
    }

    /** dd/MM/yyyy (as the other CM reports); defaults to the last 1 year. */
    private LocalDateTime[] range(String startDate, String endDate) {
        LocalDate start = (startDate != null && !startDate.isBlank())
                ? LocalDate.parse(startDate.trim(), DMY) : LocalDate.now().minusYears(1);
        LocalDate end = (endDate != null && !endDate.isBlank())
                ? LocalDate.parse(endDate.trim(), DMY) : LocalDate.now();
        return new LocalDateTime[]{start.atStartOfDay(), end.atTime(23, 59, 59)};
    }

    private List<InspectionCallsReportDto> map(List<Object[]> rows) {
        return rows.stream().map(r -> {
            InspectionCallsReportDto dto = new InspectionCallsReportDto();
            dto.setCallNumber(str(r[0]));
            dto.setProductAndStageOfInspection(str(r[1]));
            dto.setPoNumber(str(r[2]));
            dto.setDeliveryDate(toDateTime(r[3]));
            dto.setExpectedDeliveryDate(toDateTime(r[4]));
            dto.setVendorName(str(r[5]));
            dto.setInspectionDesiredDate(toDate(r[6]));
            dto.setCallDate(toDateTime(r[7]));
            dto.setIeName(str(r[8]));
            dto.setCmName(str(r[9]));
            dto.setRitesRio(str(r[10]));
            dto.setStatus(str(r[11]));
            return dto;
        }).toList();
    }

    private static String str(Object o) {
        return o != null ? o.toString() : null;
    }

    /** Accepts LocalDateTime / Timestamp / LocalDate / java.sql.Date (driver-dependent). */
    private static LocalDateTime toDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime ldt) return ldt;
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (o instanceof LocalDate ld) return ld.atStartOfDay();
        if (o instanceof java.sql.Date d) return d.toLocalDate().atStartOfDay();
        return null;
    }

    private static LocalDate toDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return null;
    }
}
