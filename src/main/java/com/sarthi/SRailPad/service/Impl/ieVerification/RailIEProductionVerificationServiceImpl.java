package com.sarthi.SRailPad.service.Impl.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.IEProductionVerificationRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.IEProductionVerificationResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo;
import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionRejection;
import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionVerification;
import com.sarthi.SRailPad.repository.ieVerification.RailIEProductionVerificationRepository;
import com.sarthi.SRailPad.service.ieVerification.RailIEProductionVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class RailIEProductionVerificationServiceImpl implements RailIEProductionVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(RailIEProductionVerificationServiceImpl.class);

    private final RailIEProductionVerificationRepository repository;
    private final com.sarthi.SRailPad.repository.inspectionCall.RailInspectionBatchRepository railInspectionBatchRepository;

    @Override
    @Transactional
    public IEProductionVerificationResponseDto create(IEProductionVerificationRequestDto requestDto) {
        System.out.println("[IE Verification] Processing request for RequestID: " + requestDto.getRequestId());
        
        RailIEProductionVerification existing = repository.findTopByRequestIdOrderByIdDesc(requestDto.getRequestId())
                .orElse(null);
        
        final RailIEProductionVerification verification;
        
        if (existing == null) {
            System.out.println("[IE Verification] Creating NEW record");
            verification = new RailIEProductionVerification();
            verification.setProductionInfos(new ArrayList<>());
            verification.setRejections(new ArrayList<>());
        } else {
            System.out.println("[IE Verification] Updating EXISTING record with ID: " + existing.getId());
            verification = existing;
            // Important: Clear the collections to trigger orphan removal
            verification.getProductionInfos().clear();
            verification.getRejections().clear();
        }

        verification.setCastingDate(requestDto.getCastingDate());
        verification.setShift(requestDto.getShift());
        verification.setProductionUnit(requestDto.getProductionUnit());
        verification.setRequestId(requestDto.getRequestId());
        verification.setTotalPiecesProduced(requestDto.getTotalPiecesProduced());
        verification.setTotalPiecesRejected(requestDto.getTotalPiecesRejected());
        verification.setTotalAcceptedPieces(requestDto.getTotalAcceptedPieces());
        verification.setCreatedBy(requestDto.getCreatedBy());
        verification.setUpdatedBy(requestDto.getCreatedBy());

        // Map Info (Child 1)
        List<RailIEProductionInfo> infos = requestDto.getProductionInfos().stream().map(dto -> {
            RailIEProductionInfo info = new RailIEProductionInfo();
            info.setProductType(dto.getProductType());
            info.setDrawingNo(dto.getDrawingNo());
            info.setBatchNo(dto.getBatchNo());
            info.setInitialWt(dto.getInitialWt());
            info.setFinalWt(dto.getFinalWt());
            info.setQuantityProduced(dto.getQuantityProduced());
            info.setVerification(verification);
            return info;
        }).collect(Collectors.toList());
        verification.getProductionInfos().addAll(infos);

        // Map Rejections (Child 2)
        List<RailIEProductionRejection> rejections = requestDto.getRejections().stream().map(dto -> {
            RailIEProductionRejection rejection = new RailIEProductionRejection();
            rejection.setProductType(dto.getProductType());
            rejection.setBatchNo(dto.getBatchNo());
            rejection.setRejectedQty(dto.getRejectedQty());
            rejection.setReason(dto.getReason());
            rejection.setVerification(verification);
            
            // Link to matching info entry if possible
            infos.stream()
                .filter(i -> i.getProductType().equals(dto.getProductType()) && i.getBatchNo().equals(dto.getBatchNo()))
                .findFirst()
                .ifPresent(rejection::setProductionInfo);
                
            return rejection;
        }).collect(Collectors.toList());
        verification.getRejections().addAll(rejections);

        RailIEProductionVerification saved = repository.saveAndFlush(verification);
        return mapToResponse(saved);
    }

    @Override
    public IEProductionVerificationResponseDto getById(Long id) {
        RailIEProductionVerification verification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification not found with id: " + id));
        return mapToResponse(verification);
    }

    @Override
    public IEProductionVerificationResponseDto getByRequestId(Long requestId) {
        RailIEProductionVerification verification = repository.findTopByRequestIdOrderByIdDesc(requestId)
                .orElseThrow(() -> new RuntimeException("Verification not found with requestId: " + requestId));
        return mapToResponse(verification);
    }

    @Override
    public List<IEProductionVerificationResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private IEProductionVerificationResponseDto mapToResponse(RailIEProductionVerification entity) {
        IEProductionVerificationResponseDto response = new IEProductionVerificationResponseDto();
        response.setId(entity.getId());
        response.setCastingDate(entity.getCastingDate());
        response.setShift(entity.getShift());
        response.setProductionUnit(entity.getProductionUnit());
        response.setRequestId(entity.getRequestId());
        response.setTotalPiecesProduced(entity.getTotalPiecesProduced());
        response.setTotalPiecesRejected(entity.getTotalPiecesRejected());
        response.setTotalAcceptedPieces(entity.getTotalAcceptedPieces());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());

        if (entity.getProductionInfos() != null) {
            response.setProductionInfos(entity.getProductionInfos().stream().map(i -> {
                IEProductionVerificationResponseDto.ProductionInfoResponseDto dto = new IEProductionVerificationResponseDto.ProductionInfoResponseDto();
                dto.setId(i.getId());
                dto.setProductType(i.getProductType());
                dto.setDrawingNo(i.getDrawingNo());
                dto.setBatchNo(i.getBatchNo());
                dto.setInitialWt(i.getInitialWt());
                dto.setFinalWt(i.getFinalWt());
                dto.setQuantityProduced(i.getQuantityProduced());
                return dto;
            }).collect(Collectors.toList()));
        }

        if (entity.getRejections() != null) {
            response.setRejections(entity.getRejections().stream().map(r -> {
                IEProductionVerificationResponseDto.ProductionRejectionResponseDto dto = new IEProductionVerificationResponseDto.ProductionRejectionResponseDto();
                dto.setId(r.getId());
                dto.setProductType(r.getProductType());
                dto.setBatchNo(r.getBatchNo());
                dto.setRejectedQty(r.getRejectedQty());
                dto.setReason(r.getReason());
                return dto;
            }).collect(Collectors.toList()));
        }
        return response;
    }
    
    @Override
    @Transactional
    public void deleteByRequestId(Long requestId) {
        repository.findTopByRequestIdOrderByIdDesc(requestId).ifPresent(verification -> {
            System.out.println("[IE Verification] Deleting record for RequestID: " + requestId);
            repository.delete(verification);
        });
    }

    @Override
    public List<com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto> getAcceptedInventory(String productionUnit, String productType) {
        System.out.println("[Accepted Inventory] Fetching for Unit: " + productionUnit + ", Type: " + productType);
        
        List<RailIEProductionVerification> verifications = repository.findAllByProductionUnit(productionUnit);
        
        // Group by casting date
        return verifications.stream()
                .collect(Collectors.groupingBy(RailIEProductionVerification::getCastingDate))
                .entrySet().stream()
                .map(entry -> {
                    com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto dto = new com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto();
                    dto.setCastingDate(entry.getKey());
                    
                    List<com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto.BatchAcceptedDto> batches = entry.getValue().stream()
                            .flatMap(v -> v.getProductionInfos().stream())
                            .filter(i -> productType == null || i.getProductType().equalsIgnoreCase(productType))
                            .map(info -> {
                                com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto.BatchAcceptedDto b = new com.sarthi.SRailPad.dto.ieVerification.RailAcceptedInventoryDto.BatchAcceptedDto();
                                b.setInfoId(info.getId());
                                b.setBatchNo(info.getBatchNo());
                                b.setProductType(info.getProductType());
                                
                                // Calculate accepted qty: produced - rejected
                                // AND subtract quantity already offered in inspection calls
                                int produced = info.getQuantityProduced() != null ? info.getQuantityProduced() : 0;
                                
                                // Check rejections in the same verification record
                                int rejected = info.getVerification().getRejections().stream()
                                        .filter(r -> r.getProductionInfo() != null && r.getProductionInfo().getId().equals(info.getId()))
                                        .mapToInt(r -> r.getRejectedQty() != null ? r.getRejectedQty() : 0)
                                        .sum();

                                // Get total offered for this batch and date across ALL calls
                                Integer offered = railInspectionBatchRepository.findTotalOfferedByBatchAndDate(
                                        info.getBatchNo(), 
                                        info.getVerification().getCastingDate()
                                );
                                int alreadyOffered = offered != null ? offered : 0;
                                
                                if (alreadyOffered > 0) {
                                    logger.info("[Inventory Stats] Batch: {}, Date: {}, Already Offered: {}", info.getBatchNo(), info.getVerification().getCastingDate(), alreadyOffered);
                                }
                                
                                b.setAcceptedQty(produced - rejected - alreadyOffered);
                                return b;
                            })
                            .filter(b -> b.getAcceptedQty() > 0)
                            .collect(Collectors.toList());
                    
                    dto.setBatches(batches);
                    return dto;
                })
                .filter(dto -> !dto.getBatches().isEmpty())
                .sorted((a, b) -> b.getCastingDate().compareTo(a.getCastingDate())) // Newest first
                .collect(Collectors.toList());
    }
}
