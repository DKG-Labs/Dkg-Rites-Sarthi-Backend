package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.EtDtos.EpoxyTreatedSleeperRequestDTO;
import com.sarthi.Sleeper.dto.EtDtos.EpoxyTreatedSleeperResponseDTO;
import com.sarthi.Sleeper.dto.EtDtos.EtBatchSummaryResponseDTO;
import com.sarthi.Sleeper.dto.EtDtos.EtSleeperDTO;
import com.sarthi.Sleeper.entity.EpoxyTreatedSleeper;
import com.sarthi.Sleeper.entity.EtSleeperDetails;
import com.sarthi.Sleeper.repository.EpoxyTreatedSleeperRepository;
import com.sarthi.Sleeper.service.EpoxyTreatedSleeperService;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EpoxyTreatedSleeperServiceImpl implements EpoxyTreatedSleeperService {

    @Autowired
    private EpoxyTreatedSleeperRepository epoxyTreatedSleeperRepository;


        @Override
        public EpoxyTreatedSleeperResponseDTO create(EpoxyTreatedSleeperRequestDTO dto) {

            EpoxyTreatedSleeper entity = new EpoxyTreatedSleeper();

            entity.setLocation(dto.getLocation());
            entity.setDateOfCasting(CommonUtils.convertStringToDateObject(dto.getDateOfCasting()));
            entity.setBatchNumber(dto.getBatchNumber());
            entity.setSleeperType(dto.getSleeperType());
            entity.setRemark(dto.getRemark());
            entity.setIsConfirmed(dto.getIsConfirmed());

            entity.setShift(dto.getShift());
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantId(dto.getPlantId());

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            List<EtSleeperDetails> list = new ArrayList<>();

            if (dto.getSleepers() != null) {
                for (EtSleeperDTO s : dto.getSleepers()) {

                    EtSleeperDetails d = new EtSleeperDetails();
                    d.setSleeperId(s.getSleeperId());
                    d.setSleeperNo(s.getSleeperNo());
                    d.setEt(entity); // IMPORTANT

                    list.add(d);
                }
            }

            entity.setSleepers(list);

            EpoxyTreatedSleeper saved = epoxyTreatedSleeperRepository.save(entity);

            return mapToResponse(saved);
        }

    @Override
    public EpoxyTreatedSleeperResponseDTO update(Long id, EpoxyTreatedSleeperRequestDTO dto) {

        EpoxyTreatedSleeper entity = epoxyTreatedSleeperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ET not found"));

        entity.setLocation(dto.getLocation());
        entity.setDateOfCasting(CommonUtils.convertStringToDateObject(dto.getDateOfCasting()));
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setSleeperType(dto.getSleeperType());
        entity.setRemark(dto.getRemark());
        entity.setIsConfirmed(dto.getIsConfirmed());

        entity.setShift(dto.getShift());
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());


        entity.getSleepers().clear();

        if (dto.getSleepers() != null) {
            for (EtSleeperDTO s : dto.getSleepers()) {

                EtSleeperDetails d = new EtSleeperDetails();
                d.setSleeperId(s.getSleeperId());
                d.setSleeperNo(s.getSleeperNo());
                d.setEt(entity);

                entity.getSleepers().add(d);
            }
        }

        EpoxyTreatedSleeper updated = epoxyTreatedSleeperRepository.save(entity);

        return mapToResponse(updated);
    }

    @Override
    public EpoxyTreatedSleeperResponseDTO getById(Long id) {

        EpoxyTreatedSleeper entity = epoxyTreatedSleeperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ET not found"));

        return mapToResponse(entity);
    }

    @Override
    public List<EpoxyTreatedSleeperResponseDTO> getAll() {

        return epoxyTreatedSleeperRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {

        if (!epoxyTreatedSleeperRepository.existsById(id)) {
            throw new RuntimeException("Record not found");
        }

        epoxyTreatedSleeperRepository.deleteById(id);
    }

    private EpoxyTreatedSleeperResponseDTO mapToResponse(EpoxyTreatedSleeper entity) {

        EpoxyTreatedSleeperResponseDTO dto = new EpoxyTreatedSleeperResponseDTO();

        dto.setId(entity.getId());
        dto.setLocation(entity.getLocation());
        dto.setDateOfCasting(CommonUtils.convertDateToString(entity.getDateOfCasting()));
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setSleeperType(entity.getSleeperType());
        dto.setRemark(entity.getRemark());
        dto.setIsConfirmed(entity.getIsConfirmed());

        dto.setShift(entity.getShift());
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        List<EtSleeperDTO> list = new ArrayList<>();

        if (entity.getSleepers() != null) {
            for (EtSleeperDetails s : entity.getSleepers()) {

                EtSleeperDTO d = new EtSleeperDTO();
                d.setSleeperId(s.getSleeperId());
                d.setSleeperNo(s.getSleeperNo());

                list.add(d);
            }
        }

        dto.setSleepers(list);

        return dto;
    }


    @Override
    public List<EtBatchSummaryResponseDTO> getAllBatchWiseEtSummary() {

        List<Object[]> list = epoxyTreatedSleeperRepository.getBatchWiseEtSummary();

        List<EtBatchSummaryResponseDTO> response = new ArrayList<>();

        for (Object[] obj : list) {

            String batch = (String) obj[0];
            String location = (String) obj[1];
            LocalDate date = (LocalDate) obj[2];

            Long total = obj[3] != null ? ((Number) obj[3]).longValue() : 0L;
            Long etCount = obj[4] != null ? ((Number) obj[4]).longValue() : 0L;

            double percentage = 0.0;
            if (total > 0) {
                percentage = (etCount * 100.0) / total;
            }

            EtBatchSummaryResponseDTO dto = new EtBatchSummaryResponseDTO();
            dto.setBatchNumber(batch);
            dto.setLocation(location);
            dto.setDateOfCasting(CommonUtils.convertDateToString(date));
            dto.setTotalSleepers(total);
            dto.setEtSleepers(etCount);
            dto.setEtPercentage(Math.round(percentage * 100.0) / 100.0);

            response.add(dto);
        }


        return response;
    }


}
