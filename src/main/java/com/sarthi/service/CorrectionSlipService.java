package com.sarthi.service;

import com.sarthi.dto.CorrectionSlipRequestDTO;
import com.sarthi.dto.CorrectionSlipResponseDTO;
import com.sarthi.entity.CorrectionSlip;
import com.sarthi.repository.CorrectionSlipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Correction Slip operations.
 * Each save replaces all existing rows for a callNo (upsert-by-callNo strategy).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CorrectionSlipService {

    private final CorrectionSlipRepository correctionSlipRepository;

    // ─── Fetch ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CorrectionSlipResponseDTO> getByCallNo(String callNo) {
        if (!StringUtils.hasText(callNo)) {
            throw new IllegalArgumentException("Call number must not be blank.");
        }
        return correctionSlipRepository
                .findByCallNoOrderByCreatedAtAsc(callNo.trim())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── Save (upsert) ───────────────────────────────────────────────────────

    @Transactional
    public List<CorrectionSlipResponseDTO> saveOrUpdateAll(CorrectionSlipRequestDTO request) {
        validateRequest(request);

        String callNo = request.getCallNo().trim();
        String createdBy = StringUtils.hasText(request.getCreatedBy())
                ? request.getCreatedBy().trim()
                : "SYSTEM";

        // Delete existing rows for this callNo, then insert fresh ones
        correctionSlipRepository.deleteAllByCallNo(callNo);

        List<CorrectionSlip> entities = request.getRows().stream()
                .map(row -> {
                    CorrectionSlip entity = new CorrectionSlip();
                    entity.setCallNo(callNo);
                    entity.setColumnName(row.getColumnName().trim());
                    entity.setReadAs(row.getReadAs().trim());
                    entity.setInsteadOf(row.getInsteadOf() != null ? row.getInsteadOf().trim() : "");
                    entity.setCreatedBy(createdBy);
                    entity.setUpdatedBy(createdBy);
                    return entity;
                })
                .collect(Collectors.toList());

        List<CorrectionSlip> saved = correctionSlipRepository.saveAll(entities);
        log.info("✅ Saved {} correction slip row(s) for call: {}", saved.size(), callNo);
        return saved.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    private void validateRequest(CorrectionSlipRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null.");
        }
        if (!StringUtils.hasText(request.getCallNo())) {
            throw new IllegalArgumentException("Call number is required.");
        }
        if (request.getRows() == null || request.getRows().isEmpty()) {
            throw new IllegalArgumentException("At least one correction row is required.");
        }
        for (int i = 0; i < request.getRows().size(); i++) {
            CorrectionSlipRequestDTO.RowDTO row = request.getRows().get(i);
            int rowNum = i + 1;
            if (row == null) {
                throw new IllegalArgumentException("Row " + rowNum + " must not be null.");
            }
            if (!StringUtils.hasText(row.getColumnName())) {
                throw new IllegalArgumentException("Row " + rowNum + ": Column name is required.");
            }
            if (!StringUtils.hasText(row.getReadAs())) {
                throw new IllegalArgumentException("Row " + rowNum + ": 'Read As' value is required.");
            }
        }
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────

    private CorrectionSlipResponseDTO toDTO(CorrectionSlip entity) {
        return CorrectionSlipResponseDTO.builder()
                .id(entity.getId())
                .callNo(entity.getCallNo())
                .columnName(entity.getColumnName())
                .readAs(entity.getReadAs())
                .insteadOf(entity.getInsteadOf())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
