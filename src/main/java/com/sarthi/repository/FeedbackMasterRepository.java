package com.sarthi.repository;

import com.sarthi.entity.FeedbackMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackMasterRepository extends JpaRepository<FeedbackMaster, Integer> {
    List<FeedbackMaster> findByUserIdOrderByCreatedDateDesc(String userId);
    List<FeedbackMaster> findAllByOrderByCreatedDateDesc();
}
