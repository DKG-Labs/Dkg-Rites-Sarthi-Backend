package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.dto.SleeperDashboardDtos.DateOnlyProjection;
import com.sarthi.Sleeper.entity.ModulusOfFailure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModulusOfFailureRepository
        extends JpaRepository<ModulusOfFailure, Long> {
    @Query("SELECT m FROM ModulusOfFailure m WHERE m.id NOT IN " +
            "(SELECT t.modulusOfFailure.id FROM MfTestDetails t)")
    List<ModulusOfFailure> findAllNotTested();

    @Query(value = """
                SELECT created_date AS createdDate
                FROM moment_of_resistance_test
                WHERE batch_number = :batchNo
                ORDER BY id DESC LIMIT 1
            """, nativeQuery = true)
    DateOnlyProjection getMFData(String batchNo);


}
