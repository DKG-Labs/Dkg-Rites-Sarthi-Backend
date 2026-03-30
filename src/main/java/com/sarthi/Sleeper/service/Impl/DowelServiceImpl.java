package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.Dowel.DowelRequestDto;
import com.sarthi.Sleeper.dto.Dowel.DowelResponseDto;
import com.sarthi.Sleeper.entity.DowelInventory;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.DowelInventoryRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.DowelService;
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
public class DowelServiceImpl implements DowelService {


    @Autowired
    private DowelInventoryRepository repository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

        // ================= CREATE =================

        @Override
        public DowelResponseDto create(DowelRequestDto dto) {

            DowelInventory entity = new DowelInventory();

            entity.setGradeType(dto.getGradeType());
            entity.setManufacturer(dto.getManufacturer());
            entity.setInvoiceNumber(dto.getInvoiceNumber());
            entity.setRitesIcNumber(dto.getRitesIcNumber());
            entity.setTotalQtyReceived(dto.getTotalQtyReceived());

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

            if (dto.getRitesIcDate() != null) {
                entity.setRitesIcDate(
                        CommonUtils.convertStringToDateObject(dto.getRitesIcDate()));
            }

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            DowelInventory saved = repository.save(entity);

            return mapToResponse(saved);
        }


        // ================= UPDATE =================

        @Override
        public DowelResponseDto update(Long id, DowelRequestDto dto) {

            DowelInventory entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Dowel not found")));

            entity.setGradeType(dto.getGradeType());
            entity.setManufacturer(dto.getManufacturer());
            entity.setInvoiceNumber(dto.getInvoiceNumber());
            entity.setRitesIcNumber(dto.getRitesIcNumber());
            entity.setTotalQtyReceived(dto.getTotalQtyReceived());

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

            if (dto.getRitesIcDate() != null) {
                entity.setRitesIcDate(
                        CommonUtils.convertStringToDateObject(dto.getRitesIcDate()));
            }

            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedDate(LocalDateTime.now());

            repository.save(entity);

            return mapToResponse(entity);
        }


        // ================= GET BY ID =================

        @Override
        public DowelResponseDto getById(Long id) {

            DowelInventory entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Dowel not found")));

            return mapToResponse(entity);
        }


        // ================= GET ALL =================

        @Override
        public List<DowelResponseDto> getAll() {

            List<DowelResponseDto> list = new ArrayList<>();

            for (DowelInventory entity : repository.findAll()) {
                list.add(mapToResponse(entity));
            }

            return list;
        }


        // ================= DELETE =================

        @Override
        public void delete(Long id) {
            DowelInventory entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Dowel not found")));

            repository.deleteById(entity.getId());

            Long moduleId = 10L;

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


        // ================= COMMON RESPONSE MAPPER =================

        private DowelResponseDto mapToResponse(DowelInventory entity) {

            DowelResponseDto response = new DowelResponseDto();

            response.setId(entity.getId());
            response.setGradeType(entity.getGradeType());
            response.setManufacturer(entity.getManufacturer());
            response.setInvoiceNumber(entity.getInvoiceNumber());
            response.setRitesIcNumber(entity.getRitesIcNumber());
            response.setTotalQtyReceived(entity.getTotalQtyReceived());
            response.setCreatedBy(entity.getCreatedBy());
            response.setPlantId(entity.getPlantId());
            response.setVendorCode(entity.getVendorCode());
            response.setCreatedDate(entity.getCreatedDate());
            response.setUpdatedBy(entity.getUpdatedBy());
            response.setUpdatedDate(entity.getUpdatedDate());

            String status = sleeperWorkflowRepository
                    .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 10L)
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

            return response;
        }

}
