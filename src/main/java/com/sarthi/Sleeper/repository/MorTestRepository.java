package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MorTestResult;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;

@ReadingConverter
public interface MorTestRepository extends JpaRepository<MorTestResult, Long> {
}