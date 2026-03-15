package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementSettingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CementSettingTimeRepository extends JpaRepository<CementSettingTime, Long> {
}
