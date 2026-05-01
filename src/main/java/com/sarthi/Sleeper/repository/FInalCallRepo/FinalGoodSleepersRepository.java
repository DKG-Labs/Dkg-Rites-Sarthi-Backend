package com.sarthi.Sleeper.repository.FInalCallRepo;

import com.sarthi.Sleeper.entity.FInalCall.FinalGoodSleepers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinalGoodSleepersRepository extends JpaRepository<FinalGoodSleepers, Long> {
}
