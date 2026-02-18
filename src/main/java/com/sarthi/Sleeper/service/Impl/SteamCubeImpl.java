package com.sarthi.Sleeper.service.Impl;


import com.sarthi.Sleeper.dto.SteamCubeDtos.SampleCubeDto;
import com.sarthi.Sleeper.dto.SteamCubeDtos.SampleOtherBenchDto;
import com.sarthi.Sleeper.dto.SteamCubeDtos.SteamCubeSampleDeclarationRequestDto;
import com.sarthi.Sleeper.dto.SteamCubeDtos.SteamCubeSampleDeclarationResponseDto;
import com.sarthi.Sleeper.entity.SampleCube;
import com.sarthi.Sleeper.entity.SampleOtherBench;
import com.sarthi.Sleeper.entity.SteamCubeSampleDeclaration;
import com.sarthi.Sleeper.repository.SteamCubeSampleDeclarationRepository;
import com.sarthi.Sleeper.service.SteamCubeService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SteamCubeImpl implements SteamCubeService {
    @Autowired
    private SteamCubeSampleDeclarationRepository steamCubeSampleDeclarationRepository;
        @Override
        public SteamCubeSampleDeclarationResponseDto create(
                SteamCubeSampleDeclarationRequestDto dto) {

            SteamCubeSampleDeclaration entity =
                    new SteamCubeSampleDeclaration();


            entity.setShedNo(dto.getShedNo());
            entity.setLineNo(dto.getLineNo());

            // Casting Date
            if (dto.getCastingDate() != null) {
                LocalDate cDate =
                        CommonUtils.convertStringToDateObject(
                                dto.getCastingDate());

                entity.setCastingDate(cDate);
            }


            if (dto.getLbcTime() != null) {
                LocalTime time =
                        LocalTime.parse(dto.getLbcTime());

                entity.setLbcTime(time);
            }

            entity.setBatchNo(dto.getBatchNo());
            entity.setConcreteGrade(dto.getConcreteGrade());
            entity.setChamberNo(dto.getChamberNo());

            if (dto.getCubes() != null) {

                List<SampleCube> cubeList =
                        new ArrayList<>();

                for (SampleCubeDto c : dto.getCubes()) {

                    SampleCube cube = new SampleCube();
                    cube.setBenchNo(c.getBenchNo());

                    cube.setSample(entity);

                    cubeList.add(cube);
                }

                entity.setCubes(cubeList);
            }

            if (dto.getOtherBenches() != null) {

                List<SampleOtherBench> benchList =
                        new ArrayList<>();

                for (SampleOtherBenchDto b : dto.getOtherBenches()) {

                    SampleOtherBench bench =
                            new SampleOtherBench();

                    bench.setSleeperSequence(b.getSleeperSequence());
                    bench.setCubeCode(b.getCubeCode());
                    bench.setBenchNo(b.getBenchNo());

                    bench.setSample(entity);

                    benchList.add(bench);
                }

                entity.setOtherBenches(benchList);
            }

            entity.setCreatedAt(LocalDateTime.now());


            SteamCubeSampleDeclaration saved =
                    steamCubeSampleDeclarationRepository.save(entity);

            return mapToResponse(saved);
        }



    @Override
    public SteamCubeSampleDeclarationResponseDto update(
            Long id,
            SteamCubeSampleDeclarationRequestDto dto) {

        SteamCubeSampleDeclaration entity =
                steamCubeSampleDeclarationRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Steam Cube Sample not found")));


        entity.setShedNo(dto.getShedNo());
        entity.setLineNo(dto.getLineNo());

        // Casting Date
        if (dto.getCastingDate() != null) {
            LocalDate cDate =
                    CommonUtils.convertStringToDateObject(
                            dto.getCastingDate());

            entity.setCastingDate(cDate);
        }


        if (dto.getLbcTime() != null) {
            LocalTime time =
                    LocalTime.parse(dto.getLbcTime());

            entity.setLbcTime(time);
        }

        entity.setBatchNo(dto.getBatchNo());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setChamberNo(dto.getChamberNo());


        if (entity.getCubes() == null) {
            entity.setCubes(new ArrayList<>());
        }

        entity.getCubes().clear();

        if (dto.getCubes() != null) {

            for (SampleCubeDto c : dto.getCubes()) {

                SampleCube cube = new SampleCube();

                cube.setBenchNo(c.getBenchNo());

                cube.setSample(entity);

                entity.getCubes().add(cube);
            }
        }


        if (entity.getOtherBenches() == null) {
            entity.setOtherBenches(new ArrayList<>());
        }

        entity.getOtherBenches().clear();

        if (dto.getOtherBenches() != null) {

            for (SampleOtherBenchDto b : dto.getOtherBenches()) {

                SampleOtherBench bench =
                        new SampleOtherBench();

                bench.setBenchNo(b.getBenchNo());

                bench.setSleeperSequence(b.getSleeperSequence());
                bench.setCubeCode(b.getCubeCode());

                bench.setSample(entity);

                entity.getOtherBenches().add(bench);
            }
        }


        SteamCubeSampleDeclaration updated =
                steamCubeSampleDeclarationRepository.save(entity);

        return mapToResponse(updated);
    }

    @Override
        public SteamCubeSampleDeclarationResponseDto getById(Long id) {

            SteamCubeSampleDeclaration entity =
                    steamCubeSampleDeclarationRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(
                                    new ErrorDetails(
                                            AppConstant.ERROR_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_VALIDATION,
                                            "Steam Cube Sample not found")));

            return mapToResponse(entity);
        }

        @Override
        public List<SteamCubeSampleDeclarationResponseDto> getAll() {

            return steamCubeSampleDeclarationRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        @Override
        public void delete(Long id) {

            SteamCubeSampleDeclaration entity =
                    steamCubeSampleDeclarationRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(
                                    new ErrorDetails(
                                            AppConstant.ERROR_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_VALIDATION,
                                            "Steam Cube Sample not found")));

            steamCubeSampleDeclarationRepository.delete(entity);
        }

    private SteamCubeSampleDeclarationResponseDto mapToResponse(
            SteamCubeSampleDeclaration entity) {

        SteamCubeSampleDeclarationResponseDto dto =
                new SteamCubeSampleDeclarationResponseDto();


        dto.setId(entity.getId());
        dto.setShedNo(entity.getShedNo());
        dto.setLineNo(entity.getLineNo());

        // Casting Date
        if (entity.getCastingDate() != null) {
            dto.setCastingDate(
                    CommonUtils.convertDateToString(
                            entity.getCastingDate()));
        }

        // LBC Time
        if (entity.getLbcTime() != null) {
            dto.setLbcTime(
                    entity.getLbcTime().toString());
        }

        dto.setBatchNo(entity.getBatchNo());
        dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setChamberNo(entity.getChamberNo());


        if (entity.getCubes() != null) {

            List<SampleCubeDto> cubeDtos =
                    new ArrayList<>();

            for (SampleCube c : entity.getCubes()) {

                SampleCubeDto cdto =
                        new SampleCubeDto();

                cdto.setBenchNo(c.getBenchNo());



                cubeDtos.add(cdto);
            }

            dto.setCubes(cubeDtos);
        }


        if (entity.getOtherBenches() != null) {

            List<SampleOtherBenchDto> benchDtos =
                    new ArrayList<>();

            for (SampleOtherBench b : entity.getOtherBenches()) {

                SampleOtherBenchDto bdto =
                        new SampleOtherBenchDto();

                bdto.setBenchNo(b.getBenchNo());

                bdto.setSleeperSequence(b.getSleeperSequence());
                bdto.setCubeCode(b.getCubeCode());

                benchDtos.add(bdto);
            }

            dto.setOtherBenches(benchDtos);
        }

        return dto;
    }

}
