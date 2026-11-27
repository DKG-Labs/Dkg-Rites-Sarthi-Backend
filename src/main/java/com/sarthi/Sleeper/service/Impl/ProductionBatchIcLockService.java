package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Final IC batch lock: a production batch stays open (unlock / modify / delete) until a Final IC
 * is issued on any call of the same plant that offered it; after that it is locked for good.
 * "IC issued" uses the same action list as the IC number freeze (SleeperWorkflowRepository.countIcIssuedActions).
 */
@Service
public class ProductionBatchIcLockService {

    /** Workflow module id of Production Declaration (same value the production declaration workflow is started with). */
    public static final Long PRODUCTION_DECLARATION_MODULE_ID = 11L;

    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;
    @Autowired
    private SleeperInspectionCallRepository sleeperInspectionCallRepository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

    public boolean isProductionDeclarationModule(Long moduleId) {
        return PRODUCTION_DECLARATION_MODULE_ID.equals(moduleId);
    }

    /**
     * Call number of the first call carrying this batch whose Final IC is issued, if any.
     * The same batch number can be declared more than once in a plant (e.g. 252 for two sleeper types), so a call
     * only counts when its sleeper type is one of this declaration's sleeper types and, when the call batch has a cast
     * date, that date is this declaration's casting date. A missing value on either side does not exclude the call.
     */
    public Optional<String> findIssuedIcCallNo(ProductionDeclaration pd) {
        return findCallNosForDeclaration(pd).stream()
                .filter(callNo -> sleeperWorkflowRepository.countIcIssuedActions(callNo) > 0)
                .findFirst();
    }

    /**
     * Calls of this declaration's plant that offered this very batch: same batch number, a sleeper type of the
     * declaration, (when the call batch has one) the same cast date, and raised on or after the casting date.
     * A missing value on either side does not exclude the call. Shared by the IC lock and the water cube lot rejection.
     */
    public List<String> findCallNosForDeclaration(ProductionDeclaration pd) {
        if (pd == null || isBlank(pd.getBatchNumber()) || isBlank(pd.getPlantId())) {
            return List.of();
        }
        List<Object[]> rows = sleeperInspectionCallRepository
                .findCallBatchesByBatchNoAndPlant(pd.getBatchNumber().trim(), pd.getPlantId().trim());
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        Set<String> pdTypes = pd.getId() == null ? Set.of()
                : productionDeclarationRepository.findSleeperTypesByDeclarationId(pd.getId()).stream()
                        .map(ProductionBatchIcLockService::normType)
                        .filter(t -> !t.isEmpty())
                        .collect(Collectors.toSet());
        LocalDate casting = pd.getCastingDate();

        Set<String> callNos = new LinkedHashSet<>();
        for (Object[] r : rows) {
            String callNo = r[0] == null ? null : r[0].toString().trim();
            if (isBlank(callNo)) continue;
            String callType = normType(r[1] == null ? null : r[1].toString());
            if (!pdTypes.isEmpty() && !callType.isEmpty() && !pdTypes.contains(callType)) continue;
            LocalDate callCast = parseDate(r[2] == null ? null : r[2].toString());
            if (casting != null && callCast != null && !casting.equals(callCast)) continue;
            // A call cannot offer sleepers cast after it was raised (batch numbers are reused across months)
            Object created = r.length > 3 ? r[3] : null;
            LocalDate callRaised = created instanceof java.time.LocalDateTime ldt ? ldt.toLocalDate()
                    : created instanceof java.sql.Timestamp ts ? ts.toLocalDateTime().toLocalDate() : null;
            if (casting != null && callRaised != null && callRaised.isBefore(casting)) continue;
            callNos.add(callNo);
        }
        return new ArrayList<>(callNos);
    }

    /** Sleeper type compared case- and space-insensitively ("WB: RT-8746" = "wb:rt-8746"). */
    private static String normType(String t) {
        return t == null ? "" : t.replaceAll("\\s+", "").toUpperCase();
    }

    /** Cast dates are stored as text: yyyy-MM-dd, dd-MM-yyyy or dd/MM/yyyy (optionally with a time). */
    private static LocalDate parseDate(String s) {
        if (isBlank(s)) return null;
        String v = s.trim();
        if (v.length() > 10) v = v.substring(0, 10);
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                return LocalDate.parse(v, f);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    /** Rejects the change when the batch is already covered by an issued Final IC. */
    public void assertEditable(ProductionDeclaration pd) {
        findIssuedIcCallNo(pd).ifPresent(callNo -> {
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_VALIDATION,
                    "Batch " + pd.getBatchNumber().trim() + " is locked: Final IC has been issued for call "
                            + callNo + ". It can no longer be unlocked, modified or deleted."));
        });
    }

    /** Same as {@link #assertEditable(ProductionDeclaration)}; a missing record keeps the existing behaviour. */
    public void assertEditable(Long productionDeclarationId) {
        if (productionDeclarationId == null) {
            return;
        }
        productionDeclarationRepository.findById(productionDeclarationId).ifPresent(this::assertEditable);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
