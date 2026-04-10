package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SteamCubeTestingDtos.SteamCubeTestingDetailsDto;
import com.sarthi.Sleeper.dto.SteamCubeTestingDtos.SteamCubeTestingRequestDto;
import com.sarthi.Sleeper.dto.SteamCubeTestingDtos.SteamCubeTestingResponseDto;
import com.sarthi.Sleeper.entity.SteamCubeT.SteamCubeTesting;
import com.sarthi.Sleeper.entity.SteamCubeT.SteamCubeTestingDetails;
import com.sarthi.Sleeper.repository.SteamCubeTestingDetailsRepository;
import com.sarthi.Sleeper.repository.SteamCubeTestingRepository;
import com.sarthi.Sleeper.service.SteamCubeTestingService;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SteamCubeTestingServiceImpl implements SteamCubeTestingService {

    @Autowired
    private SteamCubeTestingRepository steamCubeTestingRepository;

    @Autowired
    private SteamCubeTestingDetailsRepository steamCubeTestingDetailsRepository;

    @Override
    public SteamCubeTestingResponseDto create(
            SteamCubeTestingRequestDto dto) {

        SteamCubeTesting entity = new SteamCubeTesting();

        entity.setLocation(dto.getLocation());
        entity.setBatchNo(dto.getBatchNo());
        entity.setConcreteGrade(dto.getConcreteGrade());

        if (dto.getDateOfCasting() != null) {
            entity.setDateOfCasting(
                    CommonUtils.convertStringToDateObject(dto.getDateOfCasting()));
        }

        if (dto.getLbcTime() != null) {
            entity.setLbcTime(LocalTime.parse(dto.getLbcTime()));
        }

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDate.now());

        entity.setAvgStrength(dto.getAvgStrength());
        entity.setResult(dto.getResult());

        // CHILD
        List<SteamCubeTestingDetails> detailsList = new ArrayList<>();

        if (dto.getCubeDetails() != null) {
            for (SteamCubeTestingDetailsDto d : dto.getCubeDetails()) {

                SteamCubeTestingDetails child = new SteamCubeTestingDetails();

                child.setCubeNo(d.getCubeNo());

                if (d.getDateOfTesting() != null) {
                    child.setDateOfTesting(
                            CommonUtils.convertStringToDateObject(d.getDateOfTesting()));
                }

                if (d.getTime() != null) {
                    child.setTime(LocalTime.parse(d.getTime()));
                }

                child.setAgeHours(d.getAgeHours());
                child.setWeightKgs(d.getWeightKgs());
                child.setLoadKn(d.getLoadKn());
                child.setStrength(d.getStrength());

                child.setSteamCubeTesting(entity);

                detailsList.add(child);
            }
        }

        entity.setCubeDetails(detailsList);

        return mapToResponse(steamCubeTestingRepository.save(entity));
    }

    @Override
    public SteamCubeTestingResponseDto update(
            Long id,
            SteamCubeTestingRequestDto dto) {

        SteamCubeTesting entity = steamCubeTestingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Steam Cube Testing not found"));

        entity.setLocation(dto.getLocation());
        entity.setBatchNo(dto.getBatchNo());
        entity.setConcreteGrade(dto.getConcreteGrade());

        if (dto.getDateOfCasting() != null) {
            entity.setDateOfCasting(
                    CommonUtils.convertStringToDateObject(dto.getDateOfCasting()));
        }

        if (dto.getLbcTime() != null) {
            entity.setLbcTime(LocalTime.parse(dto.getLbcTime()));
        }

        // FROM FRONTEND
        entity.setAvgStrength(dto.getAvgStrength());
        entity.setResult(dto.getResult());

        // CLEAR CHILD
        if (entity.getCubeDetails() == null) {
            entity.setCubeDetails(new ArrayList<>());
        }
        entity.getCubeDetails().clear();

        // ADD AGAIN
        if (dto.getCubeDetails() != null) {
            for (SteamCubeTestingDetailsDto d : dto.getCubeDetails()) {

                SteamCubeTestingDetails child = new SteamCubeTestingDetails();

                child.setCubeNo(d.getCubeNo());
                child.setAgeHours(d.getAgeHours());
                child.setWeightKgs(d.getWeightKgs());
                child.setLoadKn(d.getLoadKn());
                child.setStrength(d.getStrength());

                child.setSteamCubeTesting(entity);

                entity.getCubeDetails().add(child);
            }
        }

        return mapToResponse(steamCubeTestingRepository.save(entity));
    }

    @Override
    public SteamCubeTestingResponseDto getById(Long id) {

        SteamCubeTesting entity = steamCubeTestingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Steam Cube Testing not found"));

        return mapToResponse(entity);
    }

    @Override
    public List<SteamCubeTestingResponseDto> getAll() {

        return steamCubeTestingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public void delete(Long id) {

        SteamCubeTesting entity = steamCubeTestingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Steam Cube Testing not found"));

        steamCubeTestingRepository.delete(entity);
    }


    @Override
    public List<SteamCubeTestingResponseDto> getByDate(
            String location,
            String batchNo,
            String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate selectedDate = LocalDate.parse(date, formatter);

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);

        // Fetch parent only (REMOVE JOIN FETCH)
        List<SteamCubeTesting> list = steamCubeTestingRepository.findByDate(
                location.trim(),
                batchNo.trim(),
                startOfDay,
                endOfDay
        );

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect IDs
        List<Long> ids = list.stream()
                .map(SteamCubeTesting::getId)
                .toList();

        //  Fetch child separately
        List<SteamCubeTestingDetails> detailsList =
                steamCubeTestingDetailsRepository.findBySteamIds(ids);


        Map<Long, List<SteamCubeTestingDetails>> detailsMap =
                detailsList.stream()
                        .collect(Collectors.groupingBy(
                                d -> d.getSteamCubeTesting().getId()
                        ));

        for (SteamCubeTesting s : list) {
            s.setCubeDetails(
                    detailsMap.getOrDefault(s.getId(), new ArrayList<>())
            );
        }

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SteamCubeTestingResponseDto mapToResponse(
            SteamCubeTesting entity) {

        SteamCubeTestingResponseDto dto =
                new SteamCubeTestingResponseDto();

        dto.setId(entity.getId());
        dto.setLocation(entity.getLocation());
        dto.setBatchNo(entity.getBatchNo());
        dto.setConcreteGrade(entity.getConcreteGrade());

        if (entity.getDateOfCasting() != null) {
            dto.setDateOfCasting(
                    CommonUtils.convertDateToString(entity.getDateOfCasting()));
        }

        if (entity.getLbcTime() != null) {
            dto.setLbcTime(entity.getLbcTime().toString());
        }

        dto.setAvgStrength(entity.getAvgStrength());
        dto.setResult(entity.getResult());

        // CHILD
        List<SteamCubeTestingDetailsDto> list = new ArrayList<>();

        if (entity.getCubeDetails() != null) {

            for (SteamCubeTestingDetails d : entity.getCubeDetails()) {

                SteamCubeTestingDetailsDto cd =
                        new SteamCubeTestingDetailsDto();

                cd.setCubeNo(d.getCubeNo());

                if (d.getDateOfTesting() != null) {
                    cd.setDateOfTesting(
                            CommonUtils.convertDateToString(d.getDateOfTesting()));
                }

                if (d.getTime() != null) {
                    cd.setTime(d.getTime().toString());
                }

                cd.setAgeHours(d.getAgeHours());
                cd.setWeightKgs(d.getWeightKgs());
                cd.setLoadKn(d.getLoadKn());
                cd.setStrength(d.getStrength());

                list.add(cd);
            }
        }

        dto.setCubeDetails(list);

        return dto;
    }
}
