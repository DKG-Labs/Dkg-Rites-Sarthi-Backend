package com.sarthi.repository;

import com.sarthi.entity.IBS.IbsCaseIntegration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IbsCaseIntegrationRepository extends JpaRepository<IbsCaseIntegration, Long> {
    List<IbsCaseIntegration> findByCompletedFalseAndNextRetryTimeBeforeAndRetryCountLessThan(LocalDateTime time, Integer retryCount, PageRequest pageable);
    boolean existsByPoKeyAndCompletedFalse(String poKey);


}
