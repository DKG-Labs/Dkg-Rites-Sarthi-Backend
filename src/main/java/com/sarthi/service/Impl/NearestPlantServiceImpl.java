package com.sarthi.service.Impl;

import com.sarthi.Sleeper.entity.VendorPlant;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository;
import com.sarthi.Sleeper.repository.VendorPlantRepository;
import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants;
import com.sarthi.SRailPad.repository.RailPoiIeMappingRepository;
import com.sarthi.SRailPad.repository.RailVendorPlantsRepository;
import com.sarthi.dto.NearestPlantDto;
import com.sarthi.dto.NearestPlantResponseDto;
import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.mfa.AuthenticatorVerification;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.repository.PoiProcessIeMappingRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.mfa.AuthenticatorVerificationRepository;
import com.sarthi.service.NearestPlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NearestPlantServiceImpl implements NearestPlantService {

    private static final double EARTH_RADIUS_M = 6371000.0;

    @Autowired private AuthenticatorVerificationRepository authVerificationRepository;
    @Autowired private UserMasterRepository userMasterRepository;
    @Autowired private PoiProcessIeMappingRepository poiProcessIeMappingRepository;
    @Autowired private PincodePoIMappingRepository pincodePoIMappingRepository;
    @Autowired private SleeperPoiIeMappingRepository sleeperPoiIeMappingRepository;
    @Autowired private VendorPlantRepository vendorPlantRepository;
    @Autowired private RailPoiIeMappingRepository railPoiIeMappingRepository;
    @Autowired private RailVendorPlantsRepository railVendorPlantsRepository;

    @Override
    public NearestPlantResponseDto findNearestPlant(String userId) {
        // 1. Get IE's latest AUTHENTICATED login location
        Optional<AuthenticatorVerification> latestAuth =
                authVerificationRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, "AUTHENTICATED");

        if (latestAuth.isEmpty() || latestAuth.get().getLatitude() == null || latestAuth.get().getLongitude() == null) {
            throw new RuntimeException("No authenticated login with location found for user: " + userId);
        }

        double ieLat = latestAuth.get().getLatitude();
        double ieLon = latestAuth.get().getLongitude();

        List<NearestPlantDto> allPlants = new ArrayList<>();

        // 2. Get user's employeeCode for ERC queries
        try {
            int numericId = Integer.parseInt(userId);
            Optional<UserMaster> user = userMasterRepository.findByUserId(numericId);

            if (user.isPresent()) {
                String employeeCode = user.get().getEmployeeCode();

                // ERC plants mapped to this Process IE
                if (employeeCode != null && !employeeCode.isEmpty()) {
                    List<String> ercPoiCodes = poiProcessIeMappingRepository
                            .findDistinctPoiCodesByEmployeeCode(employeeCode);
                    if (!ercPoiCodes.isEmpty()) {
                        List<PincodePoIMapping> ercUnits = pincodePoIMappingRepository
                                .findByPoiCodeIn(ercPoiCodes);
                        for (PincodePoIMapping unit : ercUnits) {
                            if (unit.getLatitude() != null && unit.getLongitude() != null) {
                                double dist = haversine(ieLat, ieLon,
                                        unit.getLatitude().doubleValue(),
                                        unit.getLongitude().doubleValue());
                                allPlants.add(new NearestPlantDto(
                                        "ERC",
                                        unit.getPoiCode(),
                                        unit.getUnitName(),
                                        unit.getCompanyName(),
                                        unit.getAddress(),
                                        unit.getLatitude().doubleValue(),
                                        unit.getLongitude().doubleValue(),
                                        (double) Math.round(dist)
                                ));
                            }
                        }
                    }
                }

                // Sleeper plants mapped to this Process IE
                List<SleeperPoiIeMapping> sleeperMappings = sleeperPoiIeMappingRepository
                        .findByIeUserId(numericId);
                List<String> sleeperPlantIds = sleeperMappings.stream()
                        .map(SleeperPoiIeMapping::getPlantId)
                        .filter(pid -> pid != null && !pid.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                if (!sleeperPlantIds.isEmpty()) {
                    List<VendorPlant> sleeperPlants = vendorPlantRepository.findByPlantIdIn(sleeperPlantIds);
                    for (VendorPlant plant : sleeperPlants) {
                        if (plant.getLatitude() != null && plant.getLongitude() != null) {
                            double dist = haversine(ieLat, ieLon,
                                    plant.getLatitude().doubleValue(),
                                    plant.getLongitude().doubleValue());
                            allPlants.add(new NearestPlantDto(
                                    "SLEEPER",
                                    plant.getPlantId(),
                                    plant.getPlantName(),
                                    plant.getCompanyName(),
                                    plant.getPlantAddress(),
                                    plant.getLatitude().doubleValue(),
                                    plant.getLongitude().doubleValue(),
                                    (double) Math.round(dist)
                            ));
                        }
                    }
                }

                // Rail Pad plants mapped to this Process IE
                List<RailPoiIeMapping> railMappings = railPoiIeMappingRepository.findByIeUserId(numericId);
                List<String> railPlantIds = railMappings.stream()
                        .map(RailPoiIeMapping::getPlantId)
                        .filter(pid -> pid != null && !pid.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                if (!railPlantIds.isEmpty()) {
                    List<RailVendorPlants> railPlants = railVendorPlantsRepository.findByPlantIdIn(railPlantIds);
                    for (RailVendorPlants plant : railPlants) {
                        if (plant.getLatitude() != null && plant.getLongitude() != null) {
                            double dist = haversine(ieLat, ieLon,
                                    plant.getLatitude().doubleValue(),
                                    plant.getLongitude().doubleValue());
                            allPlants.add(new NearestPlantDto(
                                    "RAIL_PAD",
                                    plant.getPlantId(),
                                    plant.getPlantName(),
                                    plant.getCompanyName(),
                                    plant.getPlantAddress(),
                                    plant.getLatitude().doubleValue(),
                                    plant.getLongitude().doubleValue(),
                                    (double) Math.round(dist)
                            ));
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid userId format: " + userId);
        }

        allPlants.sort(Comparator.comparingDouble(NearestPlantDto::getDistanceMeters));

        NearestPlantResponseDto response = new NearestPlantResponseDto();
        response.setIeLatitude(ieLat);
        response.setIeLongitude(ieLon);
        response.setAllPlants(allPlants);
        response.setNearestPlant(allPlants.isEmpty() ? null : allPlants.get(0));

        return response;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }
}
