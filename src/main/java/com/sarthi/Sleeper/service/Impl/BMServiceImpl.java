package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BenchGroupResponseDTO;
import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMDetailRequestDTO;
import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMDetailResponseDTO;
import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMRequestDTO;
import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMResponseDTO;
import com.sarthi.Sleeper.dto.BenchQueryRequestDTO;
import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMLongLineDetails;
import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMMaster;
import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMStressDetails;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.entity.StressBenchMaster;
import com.sarthi.Sleeper.repository.BMLongLineDetailsRepository;
import com.sarthi.Sleeper.repository.BMMasterRepository;
import com.sarthi.Sleeper.repository.BMStressDetailsRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.BMService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BMServiceImpl implements BMService {


        private final BMMasterRepository masterRepo;
        private final BMStressDetailsRepository stressRepo;
        private final BMLongLineDetailsRepository longLineRepo;
        @Autowired
        private SleeperWorkflowRepository sleeperWorkflowRepository;

    @Override
    public BMResponseDTO create(BMRequestDTO request) {


        BMMaster master = new BMMaster();
        master.setPlantType(request.getPlantType());
        master.setCategory(request.getCategory());
        master.setSubCategory(request.getSubCategory());
        master.setDrawingNo(request.getDrawingNo());
        master.setCreatedBy(request.getCreatedBy());
        master.setCreatedDate(new Date());

        master = masterRepo.save(master);

        if ("STRESS".equalsIgnoreCase(request.getPlantType())) {

            List<BMStressDetails> list = new ArrayList<>();

            for (var d : request.getDetails()) {

                BMStressDetails e = new BMStressDetails();

                e.setBmMaster(master);
                e.setSleeperCode(d.getSleeperCode());
                e.setSleeperDrawingNo(d.getSleeperDrawingNo());
                e.setDeclarationMode(d.getDeclarationMode());

                e.setBenchFrom(d.getBenchFrom());
                e.setBenchTo(d.getBenchTo());
                e.setBenchNumber(d.getBenchNumber());

                e.setNoOfMoulds(d.getNoOfMoulds());
                e.setCreatedBy(request.getCreatedBy());
                e.setCreatedDate(new Date());

                list.add(e);
            }

            stressRepo.saveAll(list);

        } else {

            List<BMLongLineDetails> list = new ArrayList<>();

            for (var d : request.getDetails()) {

                BMLongLineDetails e = new BMLongLineDetails();

                e.setBmMaster(master);
                e.setSleeperCode(d.getSleeperCode());
                e.setSleeperDrawingNo(d.getSleeperDrawingNo());
                e.setDeclarationMode(d.getDeclarationMode());

                e.setGangFrom(d.getGangFrom());
                e.setGangTo(d.getGangTo());
                e.setGangNumber(d.getGangNumber());

                e.setNoOfMoulds(d.getNoOfMoulds());
                e.setCreatedBy(request.getCreatedBy());
                e.setCreatedDate(new Date());

                list.add(e);
            }

            longLineRepo.saveAll(list);
        }


        return mapToResponse(master);
    }

    @Override
    public BMResponseDTO update(Long id, BMRequestDTO request) {

        BMMaster master = masterRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + id));


        master.setCategory(request.getCategory());
        master.setSubCategory(request.getSubCategory());
        master.setDrawingNo(request.getDrawingNo());
        master.setUpdatedBy(request.getCreatedBy());
        master.setUpdatedDate(new Date());

        masterRepo.save(master);

        if ("STRESS".equalsIgnoreCase(master.getPlantType())) {

            List<BMStressDetails> existing = stressRepo.findByBmMasterId(id);

            Map<Long, BMStressDetails> existingMap = new HashMap<>();
            for (BMStressDetails e : existing) {
                existingMap.put(e.getId(), e);
            }

            List<BMStressDetails> toSave = new ArrayList<>();
            List<Long> requestIds = new ArrayList<>();

            for (BMDetailRequestDTO d : request.getDetails()) {

                if (d.getId() != null && existingMap.containsKey(d.getId())) {


                    BMStressDetails e = existingMap.get(d.getId());

                    e.setSleeperCode(d.getSleeperCode());
                    e.setSleeperDrawingNo(d.getSleeperDrawingNo());
                    e.setDeclarationMode(d.getDeclarationMode());

                    e.setBenchFrom(d.getBenchFrom());
                    e.setBenchTo(d.getBenchTo());
                    e.setBenchNumber(d.getBenchNumber());

                    e.setNoOfMoulds(d.getNoOfMoulds());
                    e.setUpdatedBy(request.getCreatedBy());
                    e.setUpdatedDate(new Date());

                    toSave.add(e);
                    requestIds.add(e.getId());

                } else {


                    BMStressDetails e = new BMStressDetails();

                    e.setBmMaster(master);
                    e.setSleeperCode(d.getSleeperCode());
                    e.setSleeperDrawingNo(d.getSleeperDrawingNo());
                    e.setDeclarationMode(d.getDeclarationMode());

                    e.setBenchFrom(d.getBenchFrom());
                    e.setBenchTo(d.getBenchTo());
                    e.setBenchNumber(d.getBenchNumber());

                    e.setNoOfMoulds(d.getNoOfMoulds());
                    e.setCreatedBy(request.getCreatedBy());
                    e.setCreatedDate(new Date());

                    toSave.add(e);
                }
            }


            for (BMStressDetails e : existing) {
                if (!requestIds.contains(e.getId())) {
                    stressRepo.delete(e);
                }
            }

            stressRepo.saveAll(toSave);

        } else {

            List<BMLongLineDetails> existing = longLineRepo.findByBmMasterId(id);

            Map<Long, BMLongLineDetails> existingMap = new HashMap<>();
            for (BMLongLineDetails e : existing) {
                existingMap.put(e.getId(), e);
            }

            List<BMLongLineDetails> toSave = new ArrayList<>();
            List<Long> requestIds = new ArrayList<>();

            for (BMDetailRequestDTO d : request.getDetails()) {

                if (d.getId() != null && existingMap.containsKey(d.getId())) {


                    BMLongLineDetails e = existingMap.get(d.getId());

                    e.setSleeperCode(d.getSleeperCode());
                    e.setSleeperDrawingNo(d.getSleeperDrawingNo());
                    e.setDeclarationMode(d.getDeclarationMode());

                    e.setGangFrom(d.getGangFrom());
                    e.setGangTo(d.getGangTo());
                    e.setGangNumber(d.getGangNumber());

                    e.setNoOfMoulds(d.getNoOfMoulds());
                    e.setUpdatedBy(request.getCreatedBy());
                    e.setUpdatedDate(new Date());

                    toSave.add(e);
                    requestIds.add(e.getId());

                } else {


                    BMLongLineDetails e = new BMLongLineDetails();

                    e.setBmMaster(master);
                    e.setSleeperCode(d.getSleeperCode());
                    e.setSleeperDrawingNo(d.getSleeperDrawingNo());
                    e.setDeclarationMode(d.getDeclarationMode());

                    e.setGangFrom(d.getGangFrom());
                    e.setGangTo(d.getGangTo());
                    e.setGangNumber(d.getGangNumber());

                    e.setNoOfMoulds(d.getNoOfMoulds());
                    e.setCreatedBy(request.getCreatedBy());
                    e.setCreatedDate(new Date());

                    toSave.add(e);
                }
            }


            for (BMLongLineDetails e : existing) {
                if (!requestIds.contains(e.getId())) {
                    longLineRepo.delete(e);
                }
            }

            longLineRepo.saveAll(toSave);
        }

        return mapToResponse(master);
    }

    @Override
    public void delete(Long id) {


           BMMaster entity = masterRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bench not found with id: " + id));

            masterRepo.delete(entity);
        Long moduleId = 2L;

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

    @Override
    public BMResponseDTO getById(Long id) {

        BMMaster master = masterRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + id));

        return mapToResponse(master);
    }
    @Override
    public List<BMResponseDTO> getAll() {

        List<BMResponseDTO> responseList = new ArrayList<>();

        List<BMMaster> masters = masterRepo.findAll();

        for (BMMaster master : masters) {
            responseList.add(mapToResponse(master));
        }

        return responseList;
    }
    private BMResponseDTO mapToResponse(BMMaster master) {

        BMResponseDTO response = new BMResponseDTO();

        response.setId(master.getId());
        response.setPlantType(master.getPlantType());
        response.setCategory(master.getCategory());
        response.setSubCategory(master.getSubCategory());
        response.setDrawingNo(master.getDrawingNo());
        response.setCreatedBy(master.getCreatedBy());
        response.setCreatedDate(master.getCreatedDate());
        //response.setUpdatedBy(master.getUpdatedBy());
      //  response.setUpdatedDate(master.getUpdatedDate());

        List<BMDetailResponseDTO> detailList = new ArrayList<>();

        if ("STRESS".equalsIgnoreCase(master.getPlantType())) {

            List<BMStressDetails> list = stressRepo.findByBmMasterId(master.getId());

            for (BMStressDetails d : list) {
                detailList.add(mapStressDetail(d));
            }

        } else {

            List<BMLongLineDetails> list = longLineRepo.findByBmMasterId(master.getId());

            for (BMLongLineDetails d : list) {
                detailList.add(mapLongLineDetail(d));
            }
        }

        response.setDetails(detailList);

        return response;
    }
    private BMDetailResponseDTO mapStressDetail(BMStressDetails d) {

        BMDetailResponseDTO dto = new BMDetailResponseDTO();

        dto.setId(d.getId());
        dto.setSleeperCode(d.getSleeperCode());
        dto.setSleeperDrawingNo(d.getSleeperDrawingNo());
        dto.setDeclarationMode(d.getDeclarationMode());

        dto.setBenchFrom(d.getBenchFrom());
        dto.setBenchTo(d.getBenchTo());
        dto.setBenchNumber(d.getBenchNumber());

        dto.setNoOfMoulds(d.getNoOfMoulds());

        return dto;
    }
    private BMDetailResponseDTO mapLongLineDetail(BMLongLineDetails d) {

        BMDetailResponseDTO dto = new BMDetailResponseDTO();

        dto.setId(d.getId());
        dto.setSleeperCode(d.getSleeperCode());
        dto.setSleeperDrawingNo(d.getSleeperDrawingNo());
        dto.setDeclarationMode(d.getDeclarationMode());

        dto.setGangFrom(d.getGangFrom());
        dto.setGangTo(d.getGangTo());
        dto.setGangNumber(d.getGangNumber());

        dto.setNoOfMoulds(d.getNoOfMoulds());

        return dto;
    }


    @Override
    public List<BenchGroupResponseDTO> getBenchDetails(BenchQueryRequestDTO request) {

        List<BenchGroupResponseDTO> responseList = new ArrayList<>();

        if ("STRESS".equalsIgnoreCase(request.getPlantType())) {

            List<BMStressDetails> list =
                    stressRepo.findByBenchNumbers(request.getBenchNumbers());

            for (BMStressDetails d : list) {

                BenchGroupResponseDTO dto = new BenchGroupResponseDTO();

                Optional<BMMaster> master = masterRepo.findById(d.getBmMaster().getId());
                BMMaster bm = null;
                if(master.isPresent()){
                    bm = master.get();
                }
                dto.setBenchOrGangNumber(d.getBenchNumber());
                dto.setSleeperType(bm.getDrawingNo()+"/"+d.getSleeperCode()+ "/"+ d.getSleeperDrawingNo());
                dto.setNoOfMoulds(d.getNoOfMoulds());

                // Optional calculations
               // dto.setCount(1); // or calculate based on logic
               // dto.setRft(2.5); // static or calculate later

                responseList.add(dto);
            }

        } else {

            List<BMLongLineDetails> list =
                    longLineRepo.findByGangNumbers(request.getGangNumbers());

            for (BMLongLineDetails d : list) {

                BenchGroupResponseDTO dto = new BenchGroupResponseDTO();

                dto.setBenchOrGangNumber(d.getGangNumber());
                dto.setSleeperType(d.getSleeperCode());
                dto.setNoOfMoulds(d.getNoOfMoulds());

                dto.setCount(1);
                dto.setRft(2.5);

                responseList.add(dto);
            }
        }

        return responseList;
    }


}
