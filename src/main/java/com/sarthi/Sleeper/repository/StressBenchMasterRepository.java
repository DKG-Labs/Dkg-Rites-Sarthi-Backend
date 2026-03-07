package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.StressBenchMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StressBenchMasterRepository extends JpaRepository<StressBenchMaster, Long> {


}
