package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.LonglineRequestDTO;
import com.sarthi.Sleeper.dto.LonglineResponseDTO;
import com.sarthi.Sleeper.entity.LonglineMaster;
import com.sarthi.Sleeper.entity.SleeperDetails;
import com.sarthi.Sleeper.repository.LonglineRepository;
import com.sarthi.Sleeper.repository.SleeperDetailsRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.LonglineService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LonglineServiceImpl implements LonglineService {

    private final LonglineRepository repository;
    private final SleeperDetailsRepository sleeperDetailsRepository;

    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

    // ================= CREATE =================
    @Override
    public LonglineResponseDTO create(LonglineRequestDTO dto) {

        LonglineMaster entity = new LonglineMaster();

        entity.setCategory(dto.getCategory());
        entity.setMouldsPerGang(dto.getMouldsPerGang());
        entity.setEntryMode(dto.getEntryMode());

        if ("RANGE".equalsIgnoreCase(dto.getEntryMode())) {

            entity.setGangFrom(dto.getGangFrom());
            entity.setGangTo(dto.getGangTo());
            entity.setGangNo(null);

            if (dto.getGangFrom() != null && dto.getGangTo() != null) {
                entity.setCount(dto.getGangTo() - dto.getGangFrom() + 1);
            }

        } else {

            entity.setGangNo(dto.getGangNo());
            entity.setGangFrom(null);
            entity.setGangTo(null);
            entity.setCount(1);
        }

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        //SAVE SLEEPERS (PnC)
        if ("PnC".equalsIgnoreCase(dto.getCategory())) {

            if (dto.getSleepers() == null || dto.getSleepers().size() != 8) {
                throw new RuntimeException("PnC must have exactly 8 sleepers");
            }

            List<SleeperDetails> sleepers = dto.getSleepers().stream().map(name -> {
                SleeperDetails s = new SleeperDetails();
                s.setSleeperName(name);
                s.setLonglineMaster(entity);
                return s;
            }).toList();

            sleeperDetailsRepository.saveAll(sleepers);
        }

        return mapToResponse(entity);
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public LonglineResponseDTO update(Long id, LonglineRequestDTO dto) {

        LonglineMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        entity.setCategory(dto.getCategory());
        entity.setMouldsPerGang(dto.getMouldsPerGang());
        entity.setEntryMode(dto.getEntryMode());

        if ("RANGE".equalsIgnoreCase(dto.getEntryMode())) {

            entity.setGangFrom(dto.getGangFrom());
            entity.setGangTo(dto.getGangTo());
            entity.setGangNo(null);

            if (dto.getGangFrom() != null && dto.getGangTo() != null) {
                entity.setCount(dto.getGangTo() - dto.getGangFrom() + 1);
            }

        } else {

            entity.setGangNo(dto.getGangNo());
            entity.setGangFrom(null);
            entity.setGangTo(null);
            entity.setCount(1);
        }

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        // ================= UPDATE SLEEPERS =================
        if ("PnC".equalsIgnoreCase(dto.getCategory())) {

            if (dto.getSleepers() == null || dto.getSleepers().size() != 8) {
                throw new RuntimeException("PnC must have exactly 8 sleepers");
            }

            // fetch existing sleepers
            List<SleeperDetails> existingSleepers =
                    sleeperDetailsRepository.findByLonglineMaster(entity);

            // if no existing → insert
            if (existingSleepers.isEmpty()) {

                List<SleeperDetails> newSleepers = dto.getSleepers().stream().map(name -> {
                    SleeperDetails s = new SleeperDetails();
                    s.setSleeperName(name);
                    s.setLonglineMaster(entity);
                    return s;
                }).toList();

                sleeperDetailsRepository.saveAll(newSleepers);

            } else {

                // update existing
                for (int i = 0; i < existingSleepers.size(); i++) {
                    existingSleepers.get(i).setSleeperName(dto.getSleepers().get(i));
                }

                sleeperDetailsRepository.saveAll(existingSleepers);
            }
        }

        return mapToResponse(entity);
    }

    // ================= GET BY ID =================
    @Override
    public LonglineResponseDTO getById(Long id) {

        LonglineMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        return mapToResponse(entity);
    }

    // ================= GET ALL =================
    @Override
    public List<LonglineResponseDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {

        LonglineMaster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        sleeperDetailsRepository.deleteByLonglineMaster(entity);
        repository.deleteById(id);
    }

    // ================= MAP RESPONSE =================
    private LonglineResponseDTO mapToResponse(LonglineMaster entity) {

        LonglineResponseDTO dto = new LonglineResponseDTO();

        dto.setId(entity.getId());
        dto.setGangFrom(entity.getGangFrom());
        dto.setGangTo(entity.getGangTo());
        dto.setGangNo(entity.getGangNo());
        dto.setCount(entity.getCount());
        dto.setMouldsPerGang(entity.getMouldsPerGang());
        dto.setCategory(entity.getCategory());
        dto.setEntryMode(entity.getEntryMode());

        // FETCH SLEEPERS
        List<String> sleepers = sleeperDetailsRepository
                .findByLonglineMaster(entity)
                .stream()
                .map(SleeperDetails::getSleeperName)
                .toList();

        dto.setSleepers(sleepers);

        String status = sleeperWorkflowRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 12L)
                .orElse("NOT_STARTED");

        dto.setStatus(status);

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    }
}