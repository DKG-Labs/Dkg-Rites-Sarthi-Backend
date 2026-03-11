package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperPincodePoIMappingRepository extends JpaRepository<SleeperPincodePoIMapping, Long> {
    SleeperPincodePoIMapping findByVendorCode(String s);


    Optional<SleeperPincodePoIMapping> findByPoiCode(String poiCode);

  }
