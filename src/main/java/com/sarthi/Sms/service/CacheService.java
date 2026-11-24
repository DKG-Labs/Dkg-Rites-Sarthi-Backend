package com.sarthi.Sms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sarthi.Sms.dto.dashboard.DimensionalInspectionResDto;
public class CacheService {
    @Service
public class CacheRefreshService {

    @Autowired
    private RailMillDashboardService ds;

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void refreshDimensionalInspectionCache() {
        // Preload or refresh cache for a list of rail IDs (example)
        List<String> railIds = getRailIdsToCache(); // Fetch or define the rail IDs you want to refresh

        railIds.forEach(railId -> {
            // Load data into the cache
            List<DimensionalInspectionResDto> refreshedData =
                    ds.getDimensionalInspectionDtlByRailId(railId);

            // Update the cache manually
            cacheManager.getCache("dimensionalInspectionCache").put(railId, refreshedData);
        });

        System.out.println("Cache refreshed at " + new java.util.Date());
    }

    // Mocked method to fetch rail IDs
    private List<String> getRailIdsToCache() {
        // Replace this with logic to fetch rail IDs you want to refresh
        return List.of("Rail1", "Rail2", "Rail3");
    }
}
}
