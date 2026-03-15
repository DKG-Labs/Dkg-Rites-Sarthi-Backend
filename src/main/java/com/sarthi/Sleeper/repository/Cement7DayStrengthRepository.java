package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.Cement7DayStrength;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Cement7DayStrengthRepository extends JpaRepository<Cement7DayStrength, Long> {
}
