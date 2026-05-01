package com.sarthi.Sleeper.repository.FInalCallRepo;

import com.sarthi.Sleeper.entity.FInalCall.FinalCallRejectedSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinalCallRejectedSleeperRepository extends JpaRepository<FinalCallRejectedSleeper, Long> {
}
