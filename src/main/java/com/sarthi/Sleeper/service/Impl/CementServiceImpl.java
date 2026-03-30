package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Cement.CementBatchDetailsRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementBatchDetailsResponseDto;
import com.sarthi.Sleeper.dto.Cement.CementReceiptRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementReceiptResponseDto;
import com.sarthi.Sleeper.entity.Cement.CementBatchDetails;
import com.sarthi.Sleeper.entity.Cement.CementReceipt;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.CementReceiptRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.CementService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CementServiceImpl implements CementService {

    @Autowired
    private CementReceiptRepository repository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

        // ================= CREATE =================

        @Override
        public CementReceiptResponseDto create(CementReceiptRequestDto dto) {

            CementReceipt entity = new CementReceipt();

            entity.setGradeSpec(dto.getGradeSpec());
            entity.setManufacturer(dto.getManufacturer());
            entity.setInvoiceNumber(dto.getInvoiceNumber());
            entity.setPlantId(dto.getPlantId());
            entity.setVendorCode(dto.getVendorCode());

            if (dto.getDateOfReceipt() != null) {
                entity.setDateOfReceipt(
                        CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
            }

            if (dto.getInvoiceDate() != null) {
                entity.setInvoiceDate(
                        CommonUtils.convertStringToDateObject(dto.getInvoiceDate()));
            }

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            double total = 0;

            if (dto.getBatchDetails() != null) {
                for (CementBatchDetailsRequestDto bDto : dto.getBatchDetails()) {

                    CementBatchDetails batch = new CementBatchDetails();

                    batch.setWeekNo(bDto.getWeekNo());
                    batch.setYearNo(bDto.getYearNo());
                    batch.setMtcNo(bDto.getMtcNo());
                    batch.setQuantityKg(bDto.getQuantityKg());

                    batch.setCementReceipt(entity);

                    entity.getBatchDetails().add(batch);

                    if (bDto.getQuantityKg() != null) {
                        total += bDto.getQuantityKg();
                    }
                }
            }

            entity.setTotalQtyReceived(total);

            CementReceipt saved = repository.save(entity);

            // ===== RESPONSE BUILD =====

            CementReceiptResponseDto response = new CementReceiptResponseDto();

            response.setId(saved.getId());
            response.setGradeSpec(saved.getGradeSpec());
            response.setManufacturer(saved.getManufacturer());
            response.setInvoiceNumber(saved.getInvoiceNumber());
            response.setTotalQtyReceived(saved.getTotalQtyReceived());
            response.setCreatedBy(saved.getCreatedBy());
            response.setCreatedDate(saved.getCreatedDate());

            if (saved.getDateOfReceipt() != null) {
                response.setDateOfReceipt(
                        CommonUtils.convertDateToString(saved.getDateOfReceipt()));
            }

            if (saved.getInvoiceDate() != null) {
                response.setInvoiceDate(
                        CommonUtils.convertDateToString(saved.getInvoiceDate()));
            }

            List<CementBatchDetailsResponseDto> batchList = new ArrayList<>();

            for (CementBatchDetails batch : saved.getBatchDetails()) {

                CementBatchDetailsResponseDto bd =
                        new CementBatchDetailsResponseDto();

                bd.setId(batch.getId());
                bd.setWeekNo(batch.getWeekNo());
                bd.setYearNo(batch.getYearNo());
                bd.setMtcNo(batch.getMtcNo());
                bd.setQuantityKg(batch.getQuantityKg());

                batchList.add(bd);
            }

            response.setBatchDetails(batchList);

            return response;
        }


        // ================= UPDATE =================

        @Override
        public CementReceiptResponseDto update(Long id,
                                               CementReceiptRequestDto dto) {

            CementReceipt entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Cement not found")
                    ));

            entity.setGradeSpec(dto.getGradeSpec());
            entity.setManufacturer(dto.getManufacturer());
            entity.setInvoiceNumber(dto.getInvoiceNumber());

            entity.setPlantId(dto.getPlantId());
            entity.setVendorCode(dto.getVendorCode());

            if (dto.getDateOfReceipt() != null) {
                entity.setDateOfReceipt(
                        CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
            }

            if (dto.getInvoiceDate() != null) {
                entity.setInvoiceDate(
                        CommonUtils.convertStringToDateObject(dto.getInvoiceDate()));
            }

            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedDate(LocalDateTime.now());

            entity.getBatchDetails().clear();

            double total = 0;

            if (dto.getBatchDetails() != null) {
                for (CementBatchDetailsRequestDto bDto : dto.getBatchDetails()) {

                    CementBatchDetails batch = new CementBatchDetails();

                    batch.setWeekNo(bDto.getWeekNo());
                    batch.setYearNo(bDto.getYearNo());
                    batch.setMtcNo(bDto.getMtcNo());
                    batch.setQuantityKg(bDto.getQuantityKg());

                    batch.setCementReceipt(entity);

                    entity.getBatchDetails().add(batch);

                    if (bDto.getQuantityKg() != null) {
                        total += bDto.getQuantityKg();
                    }
                }
            }

            entity.setTotalQtyReceived(total);

            CementReceipt updated = repository.save(entity);

            CementReceiptResponseDto response = new CementReceiptResponseDto();

            response.setId(updated.getId());
            response.setGradeSpec(updated.getGradeSpec());
            response.setManufacturer(updated.getManufacturer());
            response.setInvoiceNumber(updated.getInvoiceNumber());
            response.setTotalQtyReceived(updated.getTotalQtyReceived());
            response.setUpdatedBy(updated.getUpdatedBy());
            response.setUpdatedDate(updated.getUpdatedDate());

            if (updated.getDateOfReceipt() != null) {
                response.setDateOfReceipt(
                        CommonUtils.convertDateToString(updated.getDateOfReceipt()));
            }

            if (updated.getInvoiceDate() != null) {
                response.setInvoiceDate(
                        CommonUtils.convertDateToString(updated.getInvoiceDate()));
            }

            List<CementBatchDetailsResponseDto> batchList = new ArrayList<>();

            for (CementBatchDetails batch : updated.getBatchDetails()) {

                CementBatchDetailsResponseDto bd =
                        new CementBatchDetailsResponseDto();

                bd.setId(batch.getId());
                bd.setWeekNo(batch.getWeekNo());
                bd.setYearNo(batch.getYearNo());
                bd.setMtcNo(batch.getMtcNo());
                bd.setQuantityKg(batch.getQuantityKg());

                batchList.add(bd);
            }

            response.setBatchDetails(batchList);

            return response;
        }


        // ================= GET BY ID =================

        @Override
        public CementReceiptResponseDto getById(Long id) {

            CementReceipt entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Cement not found")));
            CementReceiptResponseDto response =
                    new CementReceiptResponseDto();

            response.setId(entity.getId());
            response.setGradeSpec(entity.getGradeSpec());
            response.setManufacturer(entity.getManufacturer());
            response.setInvoiceNumber(entity.getInvoiceNumber());
            response.setTotalQtyReceived(entity.getTotalQtyReceived());
            response.setCreatedBy(entity.getCreatedBy());
            response.setUpdatedBy(entity.getUpdatedBy());
            response.setCreatedDate(entity.getCreatedDate());
            response.setUpdatedDate(entity.getUpdatedDate());

            response.setPlantId(entity.getPlantId());
            response.setVendorCode(entity.getVendorCode());
            if (entity.getDateOfReceipt() != null) {
                response.setDateOfReceipt(
                        CommonUtils.convertDateToString(entity.getDateOfReceipt()));
            }

            if (entity.getInvoiceDate() != null) {
                response.setInvoiceDate(
                        CommonUtils.convertDateToString(entity.getInvoiceDate()));
            }

            List<CementBatchDetailsResponseDto> batchList = new ArrayList<>();

            for (CementBatchDetails batch : entity.getBatchDetails()) {

                CementBatchDetailsResponseDto bd =
                        new CementBatchDetailsResponseDto();

                bd.setId(batch.getId());
                bd.setWeekNo(batch.getWeekNo());
                bd.setYearNo(batch.getYearNo());
                bd.setMtcNo(batch.getMtcNo());
                bd.setQuantityKg(batch.getQuantityKg());

                batchList.add(bd);
            }

            response.setBatchDetails(batchList);

            return response;
        }


        // ================= GET ALL =================

    @Override
    public List<CementReceiptResponseDto> getAll() {

        List<CementReceiptResponseDto> list = new ArrayList<>();

        for (CementReceipt entity : repository.findAll()) {

            CementReceiptResponseDto response =
                    new CementReceiptResponseDto();

            response.setId(entity.getId());
            response.setGradeSpec(entity.getGradeSpec());
            response.setManufacturer(entity.getManufacturer());
            response.setInvoiceNumber(entity.getInvoiceNumber());
            response.setTotalQtyReceived(entity.getTotalQtyReceived());
            response.setPlantId(entity.getPlantId());
            response.setVendorCode(entity.getVendorCode());
            response.setCreatedBy(entity.getCreatedBy());
            response.setUpdatedBy(entity.getUpdatedBy());
            response.setCreatedDate(entity.getCreatedDate());
            response.setUpdatedDate(entity.getUpdatedDate());

            if (entity.getDateOfReceipt() != null) {
                response.setDateOfReceipt(
                        CommonUtils.convertDateToString(entity.getDateOfReceipt()));
            }

            if (entity.getInvoiceDate() != null) {
                response.setInvoiceDate(
                        CommonUtils.convertDateToString(entity.getInvoiceDate()));
            }
            String status = sleeperWorkflowRepository
                    .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 6L)
                    .orElse("NOT_STARTED");
            if (status != null) {
                response.setStatus(status);
            }

            // ===== CHILD BATCH DETAILS =====

            List<CementBatchDetailsResponseDto> batchList =
                    new ArrayList<>();

            for (CementBatchDetails batch : entity.getBatchDetails()) {

                CementBatchDetailsResponseDto bd =
                        new CementBatchDetailsResponseDto();

                bd.setId(batch.getId());
                bd.setWeekNo(batch.getWeekNo());
                bd.setYearNo(batch.getYearNo());
                bd.setMtcNo(batch.getMtcNo());
                bd.setQuantityKg(batch.getQuantityKg());

                batchList.add(bd);
            }

            response.setBatchDetails(batchList);

            list.add(response);
        }

        return list;
    }


        // ================= DELETE =================

        @Override
        public void delete(Long id) {
            CementReceipt entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Cement not found")));
            repository.deleteById(entity.getId());

            Long moduleId = 6L;

            SleeperWorkflowTransaction lastWorkflow =
                    sleeperWorkflowRepository
                            .findTopByModuleIdAndRequestIdOrderByWorkflowTransitionIdDesc(
                                    moduleId,
                                    String.valueOf(entity.getId())
                            );

            SleeperWorkflowTransaction newWorkflow = new SleeperWorkflowTransaction();

            newWorkflow.setModuleId(moduleId);
            newWorkflow.setRequestId(String.valueOf(entity.getId()));

            newWorkflow.setAction("DELETE");
            newWorkflow.setStatus("DELETED");

            if (lastWorkflow != null) {
                newWorkflow.setWorkflowId(lastWorkflow.getWorkflowId());
                newWorkflow.setCurrentRole(lastWorkflow.getCurrentRole());
                newWorkflow.setNextRole(null);
                newWorkflow.setAssignedToUser(lastWorkflow.getAssignedToUser());
            }

            newWorkflow.setModifiedBy(Long.valueOf(entity.getCreatedBy()));
            newWorkflow.setCreatedDate(LocalDateTime.now());

            sleeperWorkflowRepository.save(newWorkflow);
        }

}
