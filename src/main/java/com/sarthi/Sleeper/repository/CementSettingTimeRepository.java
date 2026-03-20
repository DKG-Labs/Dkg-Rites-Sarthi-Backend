package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementSettingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CementSettingTimeRepository extends JpaRepository<CementSettingTime, Long> {
    Optional<CementSettingTime> findByRequestId(Long requestId);
}
