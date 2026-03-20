package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CementTestingStatusService {

    @Autowired
    private Cement7DayStrengthRepository cement7DayStrengthRepository;

    @Autowired
    private CementNormalConsistencyRepository cementNormalConsistencyRepository;

    @Autowired
    private CementSpecificSurfaceRepository cementSpecificSurfaceRepository;

    @Autowired
    private CementSettingTimeRepository cementSettingTimeRepository;

    @Autowired
    private CementFinenessRepository cementFinenessRepository;

    public Map<Long, String> getBulkStatus(List<Long> requestIds) {
        Map<Long, String> statusMap = new HashMap<>();
        
        for (Long requestId : requestIds) {
            boolean has7Day = cement7DayStrengthRepository.findByRequestId(requestId).isPresent();
            boolean hasNormalConsistency = cementNormalConsistencyRepository.findByRequestId(requestId).isPresent();
            boolean hasSpecificSurface = cementSpecificSurfaceRepository.findByRequestId(requestId).isPresent();
            boolean hasSettingTime = cementSettingTimeRepository.findByRequestId(requestId).isPresent();
            boolean hasFineness = cementFinenessRepository.findByRequestId(requestId).isPresent();

            if (has7Day && hasNormalConsistency && hasSpecificSurface && hasSettingTime && hasFineness) {
                statusMap.put(requestId, "Completed");
            } else {
                statusMap.put(requestId, "Pending");
            }
        }

        return statusMap;
    }
}
