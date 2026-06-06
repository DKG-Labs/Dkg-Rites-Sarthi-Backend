package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalWeightTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalWeightTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalWeightTest;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalWeightTestSample;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalWeightTestRepository;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalWeightTestSampleRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalWeightTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalWeightTestServiceImpl implements RailFinalWeightTestService {

    @Autowired
    private RailFinalWeightTestRepository repository;

    @Autowired
    private RailFinalWeightTestSampleRepository sampleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public RailFinalWeightTestResponseDto save(RailFinalWeightTestRequestDto dto) {
        RailFinalWeightTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalWeightTest());

        boolean isNew = entity.getId() == null;
        mapDtoToEntity(dto, entity);

        if (isNew) {
            entity.setCreatedBy(dto.getUserId());
            entity.setCreatedDate(LocalDateTime.now());
        } else {
            entity.setUpdatedBy(dto.getUserId());
            entity.setUpdatedDate(LocalDateTime.now());
        }

        // Save the parent first to guarantee it has an ID
        entity = repository.save(entity);

        if (!isNew) {
            // Bulk delete all old samples in a single DELETE query
            sampleRepository.deleteByWeightTestId(entity.getId());
        }

        // Batch insert the new samples in a single network round-trip using JdbcTemplate
        List<RailFinalWeightTestSample> samples = new ArrayList<>();
        if (dto.getSamples() != null && !dto.getSamples().isEmpty()) {
            String sql = "INSERT INTO rail_final_weight_test_sample (rail_final_weight_test_id, sampling_no, sample_no, sample_value, is_rejected, created_date) VALUES (?, ?, ?, ?, ?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (RailFinalWeightTestRequestDto.SampleDto sampleDto : dto.getSamples()) {
                batchArgs.add(new Object[]{
                    entity.getId(),
                    sampleDto.getSamplingNo(),
                    sampleDto.getSampleNo(),
                    sampleDto.getSampleValue(),
                    sampleDto.getIsRejected(),
                    now
                });

                // Construct entities for building the response DTO in-memory
                RailFinalWeightTestSample sampleEntity = new RailFinalWeightTestSample();
                sampleEntity.setRailFinalWeightTest(entity);
                sampleEntity.setSamplingNo(sampleDto.getSamplingNo());
                sampleEntity.setSampleNo(sampleDto.getSampleNo());
                sampleEntity.setSampleValue(sampleDto.getSampleValue());
                sampleEntity.setIsRejected(sampleDto.getIsRejected());
                sampleEntity.setCreatedDate(now);
                samples.add(sampleEntity);
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        entity.setSamples(samples);
        return buildResponse(entity);
    }

    @Override
    public RailFinalWeightTestResponseDto getById(Long id) {
        RailFinalWeightTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Weight Test record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalWeightTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalWeightTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Weight Test record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalWeightTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sampleRepository.deleteByWeightTestId(id);
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalWeightTestRequestDto dto, RailFinalWeightTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setN1(dto.getN1());
        entity.setAc1(dto.getAc1());
        entity.setRe1(dto.getRe1());
        entity.setN2(dto.getN2());
        entity.setAc2(dto.getAc2());
        entity.setRe2(dto.getRe2());
        entity.setMinWeight(dto.getMinWeight());
        entity.setMaxWeight(dto.getMaxWeight());
        entity.setIsSecondActive(dto.getIsSecondActive());

        entity.setWeightStatus(dto.getWeightStatus());
        entity.setNotOk1(dto.getNotOk1());
        entity.setNotOk2(dto.getNotOk2());
        entity.setTotalNotOk(dto.getTotalNotOk());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalWeightTestResponseDto buildResponse(RailFinalWeightTest entity) {
        RailFinalWeightTestResponseDto dto = new RailFinalWeightTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setN1(entity.getN1());
        dto.setAc1(entity.getAc1());
        dto.setRe1(entity.getRe1());
        dto.setN2(entity.getN2());
        dto.setAc2(entity.getAc2());
        dto.setRe2(entity.getRe2());
        dto.setMinWeight(entity.getMinWeight());
        dto.setMaxWeight(entity.getMaxWeight());
        dto.setIsSecondActive(entity.getIsSecondActive());

        dto.setWeightStatus(entity.getWeightStatus());
        dto.setNotOk1(entity.getNotOk1());
        dto.setNotOk2(entity.getNotOk2());
        dto.setTotalNotOk(entity.getTotalNotOk());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getSamples() != null) {
            dto.setSamples(entity.getSamples().stream().map(sample -> {
                RailFinalWeightTestResponseDto.SampleDto sampleDto = new RailFinalWeightTestResponseDto.SampleDto();
                sampleDto.setId(sample.getId());
                sampleDto.setSamplingNo(sample.getSamplingNo());
                sampleDto.setSampleNo(sample.getSampleNo());
                sampleDto.setSampleValue(sample.getSampleValue());
                sampleDto.setIsRejected(sample.getIsRejected());
                return sampleDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
