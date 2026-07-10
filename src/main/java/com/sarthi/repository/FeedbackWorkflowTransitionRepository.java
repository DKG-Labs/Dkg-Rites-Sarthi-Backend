package com.sarthi.repository;

import com.sarthi.entity.FeedbackWorkflowTransition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
      AND (
            (:vendorCode IS NOT NULL AND f.vendor_code = :vendorCode)
         OR (:createdBy IS NOT NULL AND f.created_by = :createdBy)
      )
    ORDER BY f.feedback_workflow_transition_id DESC
    """,
            nativeQuery = true)
    List<FeedbackWorkflowTransition> findFeedbackStatus(
            Integer roleId,
            String productType,
            String vendorCode,
            Integer createdBy);


    @Query(value = """
    SELECT f.*
    FROM feedback_workflow_transition f
    INNER JOIN (
        SELECT feedback_id,
               MAX(feedback_workflow_transition_id) AS max_id
        FROM feedback_workflow_transition
        GROUP BY feedback_id
    ) latest
      ON f.feedback_id = latest.feedback_id
     AND f.feedback_workflow_transition_id = latest.max_id
    WHERE f.product_type = :productType
      AND UPPER(f.next_status) <> 'CLOSED'
    ORDER BY f.feedback_workflow_transition_id DESC
    """, nativeQuery = true)
    List<FeedbackWorkflowTransition> findPendingFeedbacksByProductType(
            @Param("productType") String productType);


    @Query(value = """
    SELECT f.*
    FROM feedback_workflow_transition f
    INNER JOIN (
        SELECT feedback_id,
               MAX(feedback_workflow_transition_id) AS max_id
        FROM feedback_workflow_transition
        GROUP BY feedback_id
    ) latest
      ON f.feedback_id = latest.feedback_id
     AND f.feedback_workflow_transition_id = latest.max_id
    WHERE f.product_type = :productType
      AND UPPER(f.next_status) = 'CLOSED'
    ORDER BY f.feedback_workflow_transition_id DESC
    """, nativeQuery = true)
    List<FeedbackWorkflowTransition> findCompletedFeedbacksByProductType(
            @Param("productType") String productType);



}
