package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.VendorHtsWire.HtsCoilDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HtsCoilDetailsRepository extends JpaRepository<HtsCoilDetails, Long> {
}
