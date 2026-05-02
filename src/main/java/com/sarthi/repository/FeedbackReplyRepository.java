package com.sarthi.repository;

import com.sarthi.entity.FeedbackReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackReplyRepository extends JpaRepository<FeedbackReply, Integer> {
}
