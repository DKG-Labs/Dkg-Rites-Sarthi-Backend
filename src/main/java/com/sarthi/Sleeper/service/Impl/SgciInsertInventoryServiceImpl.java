package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertRequestDto;
import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertResponseDto;
import com.sarthi.Sleeper.entity.SgciInsertInventory;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.SgciInsertInventoryRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.SgciInsertInventoryService;
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
public class SgciInsertInventoryServiceImpl implements SgciInsertInventoryService {
    @Autowired
    private SgciInsertInventoryRepository repository;

    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

        // ================= CREATE =================

    @Override
    public SgciInsertResponseDto create(SgciInsertRequestDto dto) {

            SgciInsertInventory entity = new SgciInsertInventory();

            entity.setGradeType(dto.getGradeType());
            entity.setManufacturer(dto.getManufacturer());
            entity.setInvoiceNumber(dto.getInvoiceNumber());
            entity.setRitesIcNumber(dto.getRitesIcNumber());
            entity.setTotalQtyReceived(dto.getTotalQtyReceived());

            if (dto.getDateOfReceipt() != null) {
                entity.setDateOfReceipt(
                        CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
            }

            if (dto.getInvoiceDate() != null) {
                entity.setInvoiceDate(
                        CommonUtils.convertStringToDateObject(dto.getInvoiceDate()));
            }

            if (dto.getRitesIcDate() != null) {
                entity.setRitesIcDate(
                        CommonUtils.convertStringToDateObject(dto.getRitesIcDate()));
            }

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            SgciInsertInventory saved = repository.save(entity);

            SgciInsertResponseDto response = new SgciInsertResponseDto();

            response.setId(saved.getId());
            response.setGradeType(saved.getGradeType());
            response.setManufacturer(saved.getManufacturer());
            response.setInvoiceNumber(saved.getInvoiceNumber());
            response.setRitesIcNumber(saved.getRitesIcNumber());
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

            if (saved.getRitesIcDate() != null) {
                response.setRitesIcDate(
                        CommonUtils.convertDateToString(saved.getRitesIcDate()));
            }

            return response;
        }


        // ================= UPDATE =================

        @Override
        public SgciInsertResponseDto update(Long id, SgciInsertRequestDto dto) {

            SgciInsertInventory entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "SGCI Insert not found")));
            entity.setGradeType(dto.getGradeType());
            entity.setManufacturer(dto.getManufacturer());
            entity.setInvoiceNumber(dto.getInvoiceNumber());
            entity.setRitesIcNumber(dto.getRitesIcNumber());
            entity.setTotalQtyReceived(dto.getTotalQtyReceived());

            if (dto.getDateOfReceipt() != null) {
                entity.setDateOfReceipt(
                        CommonUtils.convertStringToDateObject(dto.getDateOfReceipt()));
            }

            if (dto.getInvoiceDate() != null) {
                entity.setInvoiceDate(
                        CommonUtils.convertStringToDateObject(dto.getInvoiceDate()));
            }

            if (dto.getRitesIcDate() != null) {
                entity.setRitesIcDate(
                        CommonUtils.convertStringToDateObject(dto.getRitesIcDate()));
            }

            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedDate(LocalDateTime.now());

            repository.save(entity);

            return getById(id);
        }


        // ================= GET BY ID =================

        @Override
        public SgciInsertResponseDto getById(Long id) {

            SgciInsertInventory entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "SGCI Insert not found")));
            SgciInsertResponseDto response = new SgciInsertResponseDto();

            response.setId(entity.getId());
            response.setGradeType(entity.getGradeType());
            response.setManufacturer(entity.getManufacturer());
            response.setInvoiceNumber(entity.getInvoiceNumber());
            response.setRitesIcNumber(entity.getRitesIcNumber());
            response.setTotalQtyReceived(entity.getTotalQtyReceived());
            response.setCreatedBy(entity.getCreatedBy());
            response.setCreatedDate(entity.getCreatedDate());
            response.setUpdatedBy(entity.getUpdatedBy());
            response.setUpdatedDate(entity.getUpdatedDate());

            if (entity.getDateOfReceipt() != null) {
                response.setDateOfReceipt(
                        CommonUtils.convertDateToString(entity.getDateOfReceipt()));
            }

            if (entity.getInvoiceDate() != null) {
                response.setInvoiceDate(
                        CommonUtils.convertDateToString(entity.getInvoiceDate()));
            }

            if (entity.getRitesIcDate() != null) {
                response.setRitesIcDate(
                        CommonUtils.convertDateToString(entity.getRitesIcDate()));
            }

            return response;
        }


        // ================= GET ALL =================

        @Override
        public List<SgciInsertResponseDto> getAll() {

            List<SgciInsertResponseDto> list = new ArrayList<>();

            for (SgciInsertInventory entity : repository.findAll()) {

                SgciInsertResponseDto response = new SgciInsertResponseDto();

                response.setId(entity.getId());
                response.setGradeType(entity.getGradeType());
                response.setManufacturer(entity.getManufacturer());
                response.setInvoiceNumber(entity.getInvoiceNumber());
                response.setRitesIcNumber(entity.getRitesIcNumber());
                response.setTotalQtyReceived(entity.getTotalQtyReceived());
                response.setCreatedBy(entity.getCreatedBy());
                response.setCreatedDate(entity.getCreatedDate());
                response.setUpdatedBy(entity.getUpdatedBy());
                response.setUpdatedDate(entity.getUpdatedDate());

                String status = sleeperWorkflowRepository
                        .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 9L)
                        .orElse("NOT_STARTED");
                if (status != null) {
                    response.setStatus(status);
                }

                if (entity.getDateOfReceipt() != null) {
                    response.setDateOfReceipt(
                            CommonUtils.convertDateToString(entity.getDateOfReceipt()));
                }

                if (entity.getInvoiceDate() != null) {
                    response.setInvoiceDate(
                            CommonUtils.convertDateToString(entity.getInvoiceDate()));
                }

                if (entity.getRitesIcDate() != null) {
                    response.setRitesIcDate(
                            CommonUtils.convertDateToString(entity.getRitesIcDate()));
                }

                list.add(response);
            }

            return list;
        }


        // ================= DELETE =================

        @Override
        public void delete(Long id) {

        SgciInsertInventory entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "SGCI Insert not found")));
        repository.deleteById(entity.getId());

            Long moduleId = 9L;

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
