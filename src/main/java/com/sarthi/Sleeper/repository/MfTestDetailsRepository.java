package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MfTestDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MfTestDetailsRepository
        extends JpaRepository<MfTestDetails, Long> {

    @Query(value = """
        SELECT *
        FROM mf_test_details
        WHERE batch_no IN (:batchNumbers)
    """, nativeQuery = true)
    List<MfTestDetails> findByBatchNumbersIn(@org.springframework.data.repository.query.Param("batchNumbers") java.util.Collection<String> batchNumbers);
}