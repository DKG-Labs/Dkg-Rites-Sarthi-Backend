package com.sarthi.controller;

import com.sarthi.dto.FeedbackDTO;
import com.sarthi.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResponseEntity<FeedbackDTO> submitFeedback(@RequestBody FeedbackDTO dto) {
        return ResponseEntity.ok(feedbackService.submitFeedback(dto));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<FeedbackDTO> replyToFeedback(
            @PathVariable Integer id, 
            @RequestBody FeedbackDTO.FeedbackReplyDTO replyDto) {
        return ResponseEntity.ok(feedbackService.replyToFeedback(id, replyDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackForUser(@PathVariable String userId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForUser(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FeedbackDTO>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }
}
