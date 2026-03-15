package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MfTestDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfTestDetailsRepository
        extends JpaRepository<MfTestDetails, Long> {
}