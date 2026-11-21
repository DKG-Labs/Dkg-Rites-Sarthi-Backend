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

import java.util.List;
import java.util.Optional;

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

    /** Call number of the first call carrying this batch whose Final IC is issued, if any. */
    public Optional<String> findIssuedIcCallNo(ProductionDeclaration pd) {
        if (pd == null || isBlank(pd.getBatchNumber()) || isBlank(pd.getPlantId())) {
            return Optional.empty();
        }
        List<String> callNos = sleeperInspectionCallRepository
                .findCallNosByBatchNoAndPlant(pd.getBatchNumber().trim(), pd.getPlantId().trim());
        if (callNos == null) {
            return Optional.empty();
        }
        return callNos.stream()
                .filter(callNo -> !isBlank(callNo))
                .filter(callNo -> sleeperWorkflowRepository.countIcIssuedActions(callNo.trim()) > 0)
                .findFirst();
    }

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
