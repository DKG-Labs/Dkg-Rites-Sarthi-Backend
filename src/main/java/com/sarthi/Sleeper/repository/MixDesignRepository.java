package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MixDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixDesignRepository extends JpaRepository<MixDesign, Long> {

    List<MixDesign> findByIdIn(List<Long> ids);
}
