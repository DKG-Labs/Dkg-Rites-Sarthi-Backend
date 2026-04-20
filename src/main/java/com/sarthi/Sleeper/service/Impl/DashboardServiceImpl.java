package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.MonthlyAnalysisDto;
import com.sarthi.Sleeper.entity.DemouldingInspection;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.entity.VendorPlant;
import com.sarthi.Sleeper.repository.DemouldingDefectiveSleeperRepository;
import com.sarthi.Sleeper.repository.DemouldingInspectionRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.InspectionTestResultRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionSleeperRepository;
import com.sarthi.Sleeper.repository.VendorPlantRepository;
import com.sarthi.Sleeper.service.DashboardService;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired
    private DemouldingDefectiveSleeperRepository demouldingDefectiveSleeperRepository;

    @Autowired
    private InspectionTestResultRepository inspectionTestResultRepository;
    @Autowired
    private ProductionSleeperRepository productionSleeperRepository;
    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;
    @Autowired
    private VendorPlantRepository vendorPlantRepository;

    @Override
    public Long getRejectedSleepersCount() {
        return demouldingDefectiveSleeperRepository.countBy();
    }

    @Override
    public Long getTotalRejectedCount() {
        return inspectionTestResultRepository.getTotalRejectedCount();
    }


    @Override
    public Double getRejectionPercentage() {

        Long productionCount = productionSleeperRepository.countBy();
        Long demouldRejected = demouldingDefectiveSleeperRepository.countBy();
        Long finalRejected = inspectionTestResultRepository.getTotalRejectedCount();

        Long totalRejected = demouldRejected + finalRejected;

        if (productionCount == 0) {
            return 0.0;
        }

        return (totalRejected * 100.0) / productionCount;
    }

    @Override
    public List<MonthlyAnalysisDto> getMonthlyAnalysis(String startDate, String endDate) {


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);


        List<Object[]> prodList = productionDeclarationRepository.getProduction(start, end);
        List<Object[]> procList = inspectionTestResultRepository.getProcessRejection(start, end);
        List<Object[]> finalList = inspectionTestResultRepository.getFinalRejection(start, end);

        List<Object[]> masterList = productionDeclarationRepository.getPlantMasterData();


        Map<String, Long> productionMap = new HashMap<>();
        Map<String, Long> processMap = new HashMap<>();
        Map<String, Long> finalMap = new HashMap<>();

        Map<String, String> plantNameMap = new HashMap<>();
        Map<String, String> rioMap = new HashMap<>();

        for (Object[] o : prodList) {
            String plantId = (String) o[0];
            productionMap.put(plantId, ((Number) o[3]).longValue());
        }

        for (Object[] o : procList) {
            processMap.put((String) o[0], ((Number) o[1]).longValue());
        }

        for (Object[] o : finalList) {
            finalMap.put((String) o[0], ((Number) o[1]).longValue());
        }

        for (Object[] o : masterList) {
            String plantId = (String) o[0];
            plantNameMap.put(plantId, (String) o[1]); // company - plantId
            rioMap.put(plantId, (String) o[2]);       // RIO
        }

        List<MonthlyAnalysisDto> result = new ArrayList<>();
        int i = 1;

        for (String plantId : plantNameMap.keySet()) {

            long production = productionMap.getOrDefault(plantId, 0L);
            long process = processMap.getOrDefault(plantId, 0L);
            long finalR = finalMap.getOrDefault(plantId, 0L);

            long acceptance = production - (process + finalR);

            double rejectionPercentage = production == 0 ? 0 :
                    ((process + finalR) * 100.0) / production;

            MonthlyAnalysisDto dto = new MonthlyAnalysisDto();
            dto.setSno(i++);
            dto.setPlantName(plantNameMap.get(plantId)); // company - plantId
            dto.setInspectedBy(rioMap.getOrDefault(plantId, "N/A")); // RIO
            dto.setProduction(production);
            dto.setProcessRejection(process);
            dto.setFinalRejection(finalR);
            dto.setAcceptance(acceptance);
            dto.setRejectionPercentage(rejectionPercentage);

            result.add(dto);
        }

        return result;
    }
}
