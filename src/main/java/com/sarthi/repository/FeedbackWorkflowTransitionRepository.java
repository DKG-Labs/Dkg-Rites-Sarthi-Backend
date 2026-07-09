package com.sarthi.repository;

import com.sarthi.entity.FeedbackWorkflowTransition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackWorkflowTransitionRepository extends JpaRepository<FeedbackWorkflowTransition, Long> {
    boolean existsByFeedbackId(String feedbackId);

    @Query(value = """
    SELECT f.*
    FROM feedback_workflow_transition f
    INNER JOIN (
        SELECT feedback_id,
               MAX(feedback_workflow_transition_id) max_id
        FROM feedback_workflow_transition
        GROUP BY feedback_id
    ) latest
        ON f.feedback_id = latest.feedback_id
       AND f.feedback_workflow_transition_id = latest.max_id
    WHERE f.next_role_id = :roleId
      AND f.product_type = :productType
    ORDER BY f.feedback_workflow_transition_id DESC
    """,
            nativeQuery = true)
    List<FeedbackWorkflowTransition> findPendingFeedbacks(
            Integer roleId,
            String productType);
}
