package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MainIeInspectionDtos.SleeperInspectionBatchDetailDTO;
import com.sarthi.Sleeper.dto.MainIeInspectionDtos.SleeperInspectionCallSummaryDTO;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.service.MainIeInspectionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainIeInspectionServiceImpl implements MainIeInspectionService {

    @Autowired
    private SleeperInspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;
    @Override
    public SleeperInspectionCallSummaryDTO getInspectionCallSummary(String callNo) {

        SleeperInspectionCall call = inspectionCallRepository
                .findByCallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        SleeperInspectionCallSummaryDTO dto = new SleeperInspectionCallSummaryDTO();

        dto.setPoNo(call.getPoNo());
        dto.setSrNo(call.getSrNo());

        dto.setSleeperType(call.getSleeperType());

        // Call Date
        if (call.getCreatedAt() != null) {
            dto.setCallDate(call.getCreatedAt().toLocalDate().toString());
        }

        // Desired Inspection Date
        if (call.getDesiredInspectionDate() != null) {
            dto.setDesiredInspectionDate(call.getDesiredInspectionDate().toString());
        }

        // Qty Offered Now
        dto.setQtyOfferedNow(call.getTotalOffered());

        // No of batches
        int batchCount = call.getBatchesSelected() != null ?
                call.getBatchesSelected().size() : 0;

        dto.setNoOfBatches(batchCount);

        // Total Rejected
        dto.setTotalRejected(call.getTotalRejected());

        // 🔥 Calculate Accepted
        int accepted = 0;

        if (call.getBatchesSelected() != null) {
            for (SleeperInspectionCallBatch batch : call.getBatchesSelected()) {

                if (batch.getGoodSleepers() != null) {
                    accepted += batch.getGoodSleepers().size();
                }
            }
        }

        dto.setTotalAccepted(accepted);

        // Optional fields
        dto.setQuantityOnOrder(null);
        dto.setCumulativeQtyOffered(0);
        dto.setCumulativeQtyPassed(0);

        // ET sleepers (as per requirement)
        dto.setNoOfEtSleepers(null);

        return dto;
    }

    @Override
    public List<SleeperInspectionBatchDetailDTO> getBatchWiseDetails(String callNo) {

        SleeperInspectionCall call = inspectionCallRepository
                .findByCallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        List<SleeperInspectionBatchDetailDTO> response = new ArrayList<>();

        for (SleeperInspectionCallBatch batch : call.getBatchesSelected()) {

            SleeperInspectionBatchDetailDTO dto = new SleeperInspectionBatchDetailDTO();

            String batchNo = batch.getBatchNo();
            dto.setBatchNo(batchNo);


            ProductionDeclaration declaration =
                    productionDeclarationRepository.findByBatchNumber(batchNo);

            dto.setCastingDate(declaration.getCastingDate().toString());
            Integer totalCasted = declaration.getTotalCastedSleepers();
            dto.setTotalSleepersCasted(totalCasted);

            List<String> accepted = batch.getGoodSleepers() != null
                    ? batch.getGoodSleepers().stream().map(SleeperDetail::getSleeperNo).collect(Collectors.toList())
                    : new ArrayList<>();

            List<String> rejected = batch.getBadSleepers() != null
                    ? batch.getBadSleepers().stream().map(SleeperDetail::getSleeperNo).collect(Collectors.toList())
                    : new ArrayList<>();

            dto.setAcceptedSleepers(accepted);
            dto.setRejectedSleepers(rejected);

            int passed = accepted.size();
            int rejectedCount = rejected.size();

            dto.setPassed(passed);
            dto.setRejected(rejectedCount);

            int offeredNow = passed + rejectedCount;
            dto.setOfferedNow(offeredNow);

            int unoffered = totalCasted - offeredNow;
            dto.setUnoffered(unoffered);

            dto.setEtSleepers(null);

            response.add(dto);
        }

        return response;
    }
}
