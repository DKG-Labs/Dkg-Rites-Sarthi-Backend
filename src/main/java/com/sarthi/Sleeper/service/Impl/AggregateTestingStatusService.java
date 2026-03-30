package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AggregateTestingStatusService {

    @Autowired
    private Aggregate10mmQualityRepository aggregate10mmQualityRepository;

    @Autowired
    private Aggregate20mmQualityRepository aggregate20mmQualityRepository;

    @Autowired
    private AggregateFlakinessRepository aggregateFlakinessRepository;

    @Autowired
    private AggregateGranulometricRepository aggregateGranulometricRepository;

    @Autowired
    private AggregateSoundnessRepository aggregateSoundnessRepository;

    public Map<Long, String> getBulkStatus(List<Long> requestIds) {
        Map<Long, String> statusMap = new HashMap<>();

        for (Long requestId : requestIds) {
            boolean has10mm = aggregate10mmQualityRepository.findByRequestId(requestId).isPresent();
            boolean has20mm = aggregate20mmQualityRepository.findByRequestId(requestId).isPresent();
            boolean hasFlakiness = aggregateFlakinessRepository.findByRequestId(requestId).isPresent();
            boolean hasGranulometric = aggregateGranulometricRepository.findByRequestId(requestId).isPresent();
            boolean hasSoundness = aggregateSoundnessRepository.findByRequestId(requestId).isPresent();

            if (has10mm && has20mm && hasFlakiness && hasGranulometric && hasSoundness) {
                statusMap.put(requestId, "Completed");
            } else {
                statusMap.put(requestId, "Pending");
            }
        }

        return statusMap;
    }
}
